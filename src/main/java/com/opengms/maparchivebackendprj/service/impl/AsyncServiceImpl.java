package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.service.IAsyncService;
import com.opengms.maparchivebackendprj.service.IMapItemService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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


    @Async
    @Override
    public void generateThumbnailImage(String mapItemId, String inputPath, String outputDir) {

        MapItem mapItem = mapItemDao.findById(mapItemId);
        mapItem.setProcessStatus(StatusEnum.Started);
        mapItem.setThumbnailStatus(StatusEnum.Started);
        mapItemDao.save(mapItem);

        // ImageUtils.thumbnailImage(inputPath, outputDir, 200, 200,"thumb_",false);
        File inputFile = new File(inputPath);
        try {
            // Thumbnails.of(inputFile)
            //     .size(200, 200)
            //     .toFile(outputDir + "/thumb_" + inputFile.getName());
            String filename = FileUtils.getFilenameNoSuffix(inputFile);
            //先创建目录
            File output = new File(outputDir);
            if (!output.exists()) {
                output.mkdirs();
            }
            String outputPath = outputDir + "/thumb_" + filename + ".png";
            log.info("start generate thumbnail, path:{}",outputPath);
            //生成缩略图(输出路径的父文件必须存在)
            Thumbnails.of(inputPath)
                .size(200, 200)
                .toFile(outputPath);

            // 执行完耗时方法要再请求一次数据库，防止该条目的信息不是最新的
            mapItem = mapItemDao.findById(mapItem.getId());

        }catch (Exception e){
            log.error(String.valueOf(e));
            mapItem.setThumbnailStatus(StatusEnum.Error);
            mapItem.setProcessStatus(StatusEnum.Error);
            mapItemDao.save(mapItem);
            return;
        }

        mapItem.setThumbnailStatus(StatusEnum.Finished);
        mapItem.setProcessStatus(StatusEnum.Finished);

        if (hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        mapItemDao.save(mapItem);

    }

    @Async
    @Override
    public void generateTiles(String mapItemId, String inputPath, String outputDir) {

        MapItem mapItem = mapItemDao.findById(mapItemId);
        mapItem.setProcessStatus(StatusEnum.Started);
        //更新数据库中的切片状态
        mapItem.setTileStatus(StatusEnum.Started);
        mapItemDao.save(mapItem);

        //存储瓦片
        long l = ImageUtils.image2Tiles(inputPath, outputDir, resourcePath);

        // 执行完耗时方法要再请求一次数据库，防止该条目的信息不是最新的
        mapItem = mapItemDao.findById(mapItem.getId());

        // 如果输出文件夹不存在或者执行的时间小于1s的话就说明切片有问题
        File file = new File(outputDir);
        // if ((!file.exists() && !file.isDirectory()) || l < 1){
        if (!file.exists() && !file.isDirectory()){
            mapItem.setTileStatus(StatusEnum.Error);
            mapItem.setProcessStatus(StatusEnum.Error);
        } else {
            mapItem.setTileStatus(StatusEnum.Finished);
            mapItem.setProcessStatus(StatusEnum.Finished);
        }

        if (hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        mapItemDao.save(mapItem);

    }

    @Override
    public boolean hasProcessFinish(MapItem mapItem){

        StatusEnum processStatus = mapItem.getProcessStatus();
        StatusEnum thumbnailStatus = mapItem.getThumbnailStatus();
        StatusEnum tileStatus = mapItem.getTileStatus();
        boolean hasMatchMetaData = mapItem.isHasMatchMetaData();
        boolean hasCalcCoordinate = mapItem.isHasCalcCoordinate();

        if(processStatus == StatusEnum.Finished &&
            thumbnailStatus == StatusEnum.Finished &&
            tileStatus == StatusEnum.Finished &&
            hasMatchMetaData && hasCalcCoordinate){
            return true;
        }

        return false;

    }
}
