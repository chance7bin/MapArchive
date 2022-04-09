package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import org.springframework.data.domain.Pageable;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IGenericService {

    Pageable getPageable(FindDTO findDTO);

    //验证FindDTO的参数是否正确
    boolean checkFindDTOParams(FindDTO findDTO);

    boolean isEmptyString(String s);

    boolean isNullParam(Object o);

    boolean isExist(Object o);

    //根据传入的server name得到资源存储的路径
    String getLoadPath(String serverName);

    //没找到的时候返回的是null
    String getLoadPathReturnNull(String serverName);

    MapItem matchMetadata(MapItem mapItem, String mapCLSId, String metadataExcelPath);

    MapItem setItemGeo(MapItem mapItem, String mapCLSId);

    /**
     * 生成缩略图
     * @param mapItem 地图
     * @param inputPath 输入文件路径
     * @param outputDir 输出文件夹路径
     * @return void
     * @Author bin
     **/
    MapItem generateThumbnailImage(MapItem mapItem, String inputPath, String outputDir);

    /**
     * 切片
     * @param mapItem 地图
     * @param inputPath 输入文件路径
     * @param outputDir 输出文件夹路径
     * @return void
     * @Author bin
     **/
    MapItem generateTiles(MapItem mapItem, String inputPath, String outputDir);

    //判断该条目是否处理完成
    boolean hasProcessFinish(MapItem mapItem);


}
