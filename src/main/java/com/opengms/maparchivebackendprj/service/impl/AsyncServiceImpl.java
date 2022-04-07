package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ProcessParam;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.BatchProcessDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.ProcessDTO;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.service.IAsyncService;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IMapItemService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    @Autowired
    IMapItemDao mapItemDao;

    @Value("${resourcePath}")
    private String resourcePath;

    @Resource(name="defaultDataServer")
    DataServer defaultDataServer;

    @Autowired
    IGenericService genericService;


    @Async
    @Override
    public void initMapItem(
        MapItem mapItem, String loadPath,
        String savePath, String rootPath,
        ProcessDTO processDTO, FileInfo fileInfo) {


        // MapItem mapItem = new MapItem();
        mapItem.setProcessStatus(StatusEnum.Started);

        //文件与地图条目进行关联
        if(fileInfo != null){
            mapItem.setRelativeFileId(fileInfo.getId());
        }


        if (processDTO.isMatchMetadata()){
            mapItem = genericService.matchMetadata(mapItem,mapItem.getMapCLSId(),processDTO.getMetadataExcelPath());

        }

        if (processDTO.isCalcGeoInfo()){
            mapItem = genericService.setItemGeo(mapItem, mapItem.getMapCLSId());
        }


        String type = FileUtils.getFileType(mapItem.getName());
        // 执行脚本的文件后缀必须为 .jpg 或者是 .png  或者是.tif .tiff
        if (type.equals("jpg") || type.equals("png")
            || type.equals("tif") || type.equals("tiff")
            || type.equals("TIF") || type.equals("TIFF")){

            //加载的文件路径
            // String filePath = loadPath;
            //新文件保存路径
            String thumbnailPath = "/" + "thumbnail";
            String tilesPath = "/" + "tiles";
            //对多级目录的处理
            String[] path = FileUtils.buildMultiDirPath(loadPath, rootPath, thumbnailPath, tilesPath);

            File loadFile = new File(loadPath);

            // 得到图片的元数据
            try {
                // TODO: 2022/3/16 正常图片的宽度应该大于高度，如果不是这样的话可能图片需要做旋转的处理
                mapItem.setImageMetadata(ImageUtils.getImageInfo(loadPath));
            } catch (Exception e){
                mapItem.setImageMetadata(null);
            }

            path[1] += "/" + FileUtils.getFilenameNoSuffix(loadFile);

            // 异步处理地图
            if (processDTO.isGenerateThumbnail()){
                mapItem = genericService.generateThumbnailImage(mapItem,loadPath,savePath + path[0]);
            }

            if (processDTO.isGenerateTiles()){
                mapItem = genericService.generateTiles(mapItem,loadPath,savePath + path[1]);
            }

            // 如果既没有生成缩略图也没有切片，那处理的状态设置为Finished
            mapItem.setProcessStatus(StatusEnum.Finished);

            if (genericService.hasProcessFinish(mapItem)){
                mapItem.setHasNeedManual(false);
            }


            // 设置生成的图片路径
            // mapItem中存的都是相对路径，定位文件位置用rootPath定位
//            String originalUrl = fileInfo.getPath().replace("\\", "/");
//            savePath = savePath.replace("\\", "/");
//             mapItem.getImageUrl().setOriginalUrl(loadPath);
            loadPath = loadPath.replace("\\", "/");
            mapItem.getImageUrl().setOriginalUrl(loadPath.split(defaultDataServer.getLoadPath())[1]);
            String filename = FileUtils.getFilenameNoSuffix(loadFile);
            filename = "thumb_" + filename + ".png";
            mapItem.getImageUrl().setThumbnailUrl((savePath + path[0] + "/" + filename).split(defaultDataServer.getLoadPath())[1]);
            mapItem.getImageUrl().setTilesDir((savePath + path[1]).split(defaultDataServer.getLoadPath())[1]);
            // mapItem.setRootPath(savePath);
            // mapItem.setResourceDir(savePath.split(resourcePath)[1]);
            // mapItem.setResourceDir(savePath.split(defaultDataServer.getLoadPath())[1]);
            mapItem.setServer(defaultDataServer.getName());
            mapItem.setProcessParam(
                new ProcessParam(
                    mapItem.getId(),
                    loadPath.split(defaultDataServer.getLoadPath())[1],
                    (savePath + path[0]).split(defaultDataServer.getLoadPath())[1],
                    (savePath + path[1]).split(defaultDataServer.getLoadPath())[1]
                )
            );
        }


        mapItemDao.save(mapItem);


    }

    @Async
    @Override
    public void batchProcess(MapItem mapItem, BatchProcessDTO processDTO) {

        ProcessParam param = mapItem.getProcessParam();

        String loadPath = genericService.getLoadPath(mapItem.getServer());

        mapItem.setProcessStatus(StatusEnum.Started);

        if (processDTO.isMatchMetadata() && !mapItem.isHasMatchMetaData()){
            mapItem = genericService.matchMetadata(mapItem, mapItem.getMapCLSId(), null);
        }

        if (processDTO.isCalcGeoInfo() && !mapItem.isHasCalcCoordinate()){
            mapItem = genericService.setItemGeo(mapItem,mapItem.getMapCLSId());
        }

        if (processDTO.isGenerateThumbnail() && (mapItem.getThumbnailStatus() != StatusEnum.Finished)){
            mapItem = genericService.generateThumbnailImage(mapItem,loadPath + param.getInputPath(),loadPath + param.getThumbnailOutputDir());
        }

        if (processDTO.isGenerateTiles() && (mapItem.getTileStatus() != StatusEnum.Finished)){
            mapItem = genericService.generateTiles(mapItem,loadPath + param.getInputPath(),loadPath + param.getTilesOutputDir());
        }

        mapItem.setProcessStatus(StatusEnum.Finished);

        if (genericService.hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        mapItemDao.save(mapItem);

    }
}
