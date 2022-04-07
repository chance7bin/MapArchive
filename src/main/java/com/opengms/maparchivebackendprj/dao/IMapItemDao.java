package com.opengms.maparchivebackendprj.dao;

import com.mongodb.client.result.DeleteResult;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Box;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPolygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IMapItemDao {

    MapItem insert(MapItem mapItem);

    MapItem save(MapItem mapItem);

    MapItem findById(String mapItemId);

    List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, List<String> clsIdList, Pageable pageable);

    List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        Box box, List<String> clsIdList, Pageable pageable);

    List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonMultiPolygon polygon, List<String> clsIdList, Pageable pageable);

    long countBySearchTextAndPolygon(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, List<String> clsIdList);

    long countBySearchTextAndPolygon(
        String curQueryField, String searchText,
        GeoJsonMultiPolygon polygon, List<String> clsIdList);

    List<MapItem> findBySearchTextAndPageable(String curQueryField, String searchText, List<String> clsIdList, Pageable pageable);


    long countBySearchText(String curQueryField, String searchText,List<String> clsIdList);

    //根据处理状态查
    List<MapItem> findBySearchTextAndStatus(String curQueryField, String searchText,List<StatusEnum> statusEnums, List<String> clsIdList, Pageable pageable);

    List<MapItem> findByStatus(List<StatusEnum> statusEnums,Pageable pageable);
    List<MapItem> findByStatus(List<StatusEnum> statusEnums);

    List<MapItem> findByStatusAndHasNeedManual(List<StatusEnum> statusEnums,boolean hasNeedManual,Pageable pageable);

    List<MapItem> findByStatusAndHasNeedManual(String curQueryField, String searchText,List<StatusEnum> statusEnums, boolean hasNeedManual, List<String> clsIdList, Pageable pageable);

    List<MapItem> findByHasNeedManual(String curQueryField, String searchText,boolean hasNeedManual, List<String> clsIdList, Pageable pageable);

    long countByStatus(String curQueryField, String searchText,List<StatusEnum> statusEnums, List<String> clsIdList);

    long countByStatusAndHasNeedManual(String curQueryField, String searchText,List<StatusEnum> statusEnums, boolean hasNeedManual, List<String> clsIdList);

    List<MapItem> findAll(Pageable pageable);


    DeleteResult delete(MapItem mapItem);
}
