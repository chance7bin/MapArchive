package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServerList;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IGeoInfoService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Slf4j
@Service
public class GenericServiceImpl implements IGenericService {

    @Resource(name = "dataServerList")
    List<DataServer> dataServerList;

    @Resource(name = "defaultDataServer")
    DataServer defaultDataServer;

    @Autowired
    IMapItemDao mapItemDao;

    @Value("${resourcePath}")
    private String resourcePath;

    @Autowired
    IMetadataService metadataService;

    @Autowired
    IGeoInfoService geoInfoService;


    @Override
    public Pageable getPageable(FindDTO findDTO) {
        return PageRequest.of(findDTO.getPage()-1, findDTO.getPageSize(), Sort.by(findDTO.getAsc()? Sort.Direction.ASC: Sort.Direction.DESC,findDTO.getSortField()));
    }

    @Override
    public boolean checkFindDTOParams(FindDTO findDTO) {

        return isExist(findDTO.getAsc()) &&
            isExist(findDTO.getPage()) &&
            isExist(findDTO.getPageSize()) &&
            isExist(findDTO.getSortField());
    }

    @Override
    public boolean isEmptyString(String s) {
        return (s == null || s.equals(""));
    }

    @Override
    public boolean isNullParam(Object o) {
        return o == null ;
    }

    @Override
    public boolean isExist(Object o) {
        return o != null;
    }

    @Override
    public String getLoadPath(String serverName) {

        for (DataServer dataServer : dataServerList) {

            if (dataServer.getName().equals(serverName)){
                return dataServer.getLoadPath();
            }

        }
        // return null;
        return defaultDataServer.getLoadPath();

    }

    @Override
    public String getLoadPathReturnNull(String serverName) {
        for (DataServer dataServer : dataServerList) {

            if (dataServer.getName().equals(serverName)){
                return dataServer.getLoadPath();
            }

        }
        return null;
    }

    @Override
    public MapItem matchMetadata(MapItem mapItem, String mapCLSId, String metadataExcelPath) {
        Map<String, Object> metadataByName = null;
        try {
            metadataByName = metadataService.getMetadataByFilenameByType(mapItem.getName(), mapCLSId, metadataExcelPath);
        }catch (Exception e){
            mapItem.setHasMatchMetaData(false);
            return mapItem;
        }
        if (metadataByName == null){
            mapItem.setHasMatchMetaData(false);
            // mapItem.setHasNeedManual(true);
        }else {
            mapItem.setMetadata(metadataByName);
            mapItem.setHasMatchMetaData(true);
        }

        return mapItem;
    }

    @Override
    public MapItem setItemGeo(MapItem mapItem, String mapCLSId) {
        GeoInfo coordinate;

        try {
            String fileName = (String) mapItem.getMetadata().get("档号");
            coordinate = geoInfoService.getCoordinate(fileName, mapCLSId);
        }catch (Exception e){
            coordinate = null;
        }

        if (coordinate != null){
            mapItem.setCenter(coordinate.getCenter());
            mapItem.setPolygon(coordinate.getPolygon());
            mapItem.setHasCalcCoordinate(true);
            // mapItem.setHasNeedManual(false);
        } else {
            mapItem.setHasCalcCoordinate(false);
            // mapItem.setHasNeedManual(true);
        }

        return mapItem;
    }

    @Override
    public MapItem generateThumbnailImage(MapItem mapItem, String inputPath, String outputDir) {
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
            // mapItem = mapItemDao.findById(mapItem.getId());

        }catch (Exception e){
            log.error(String.valueOf(e));
            mapItem.setThumbnailStatus(StatusEnum.Error);
            mapItem.setProcessStatus(StatusEnum.Error);

            return mapItemDao.save(mapItem);
        }

        mapItem.setThumbnailStatus(StatusEnum.Finished);
        mapItem.setProcessStatus(StatusEnum.Finished);


        if (hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        return mapItemDao.save(mapItem);
    }

    @Override
    public MapItem generateTiles(MapItem mapItem, String inputPath, String outputDir) {
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

        return mapItemDao.save(mapItem);
    }

    @Override
    public boolean hasProcessFinish(MapItem mapItem) {
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
