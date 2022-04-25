package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ScaleCoordinate;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IGeoInfoService {

    /**
     * 根据文件名和对应的分类得到地理信息
     * @param filename 文件名(废弃) 该字段新的传入的参数是元数据表中的"档号"
     * @param mapCLSId 分类id
     * @return com.opengms.maparchivebackend.entity.bo.mapItem.GeoInfo
     * @Author bin
     **/
    // TODO: 2022/3/24 改成直接根据文件名进行计算
    GeoInfo getCoordinate(String filename, String mapCLSId);

    GeoInfo getGeoInfo(ScaleCoordinate coordinate);
}
