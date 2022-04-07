package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.ExcelPathDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;

import java.util.Map;

/**
 * @Description 元数据匹配
 * @Author bin
 * @Date 2022/03/24
 */
public interface IMetadataService {

    /**
     * mapItem调用的方法入口，传入地图类型以及地图的分类
     * @param filename 文件名（包括文件后缀）
     * @param mapCLSId 地图类型
     * @param excelPath 传入的excel表路径
     * @return java.util.List<java.lang.String>
     * @Author bin
     **/
    Map<String, Object> getMetadataByFilenameByType(String filename, String mapCLSId, String excelPath);

    JsonResult getMetadataByExcel(ExcelPathDTO excelPathDTO);

    JsonResult getMetadata(SpecificFindDTO findDTO, String collection);
}
