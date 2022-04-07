package com.opengms.maparchivebackendprj.dao;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IMetadataDao {

    // // 文件名和原图幅编号匹配
    // List<JSONObject> findMetadataByOriginalNum(String filename, MapClassification mapCLS);
    //
    // // 文件名和(原图幅编号+年份)匹配
    // List<JSONObject> findMetadataByOriginalNumAndYear(String filename, MapClassification mapCLS);
    //
    // List<JSONObject> findMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS, Pageable pageable);
    //
    // long countMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS);

    // 文件名和原图幅编号匹配
    List<JSONObject> findMetadataByOriginalNum(String filename, String collection);

    // 文件名和(原图幅编号+年份)匹配
    List<JSONObject> findMetadataByOriginalNumAndYear(String filename, String collection);

    List<JSONObject> findMetadataBySearchText(String curQueryField, String searchText, String collection, Pageable pageable);

    long countMetadataBySearchText(String curQueryField, String searchText,  String collection);

}
