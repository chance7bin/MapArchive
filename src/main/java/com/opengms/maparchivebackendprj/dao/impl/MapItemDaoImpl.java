package com.opengms.maparchivebackendprj.dao.impl;

import com.mongodb.client.result.DeleteResult;
import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Box;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPolygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Repository("mapItemDaoImpl")
public class MapItemDaoImpl implements IMapItemDao {

    @Autowired
    MongoTemplate mongoTemplate;


    @Override
    public MapItem insert(MapItem mapItem) {
        return mongoTemplate.insert(mapItem);
    }

    @Override
    public MapItem save(MapItem mapItem) {
        return mongoTemplate.save(mapItem);
    }

    @Override
    public MapItem findById(String mapItemId) {
        return mongoTemplate.findById(mapItemId, MapItem.class);
    }

    @Override
    public List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, List<String> clsIdList, Pageable pageable) {

        Query query = new Query();
        // query.addCriteria(Criteria.where("box").within(box));
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("polygon").intersects(polygon));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));

        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        Box box, List<String> clsIdList, Pageable pageable) {

        Query query = new Query();
        query.addCriteria(Criteria.where("box").within(box));
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));

        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public List<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonMultiPolygon polygon, List<String> clsIdList, Pageable pageable) {
        Query query = new Query();
        // query.addCriteria(Criteria.where("box").within(box));
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("polygon").intersects(polygon));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        List<MapItem> mapItemList = mongoTemplate.find(query.with(pageable), MapItem.class);
        return mapItemList;
    }

    @Override
    public long countBySearchTextAndPolygon(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, List<String> clsIdList) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("polygon").intersects(polygon));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.count(query,MapItem.class);
    }

    @Override
    public long countBySearchTextAndPolygon(
        String curQueryField, String searchText,
        GeoJsonMultiPolygon polygon, List<String> clsIdList) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("polygon").intersects(polygon));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.count(query,MapItem.class);
    }

    @Override
    public List<MapItem> findBySearchTextAndPageable(String curQueryField, String searchText, List<String> clsIdList, Pageable pageable) {

        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));

        return mongoTemplate.find(query.with(pageable), MapItem.class);


    }

    @Override
    public long countBySearchText(String curQueryField, String searchText, List<String> clsIdList) {

        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.count(query,MapItem.class);


    }

    @Override
    public List<MapItem> findBySearchTextAndStatus(String curQueryField, String searchText, List<StatusEnum> statusEnums, List<String> clsIdList, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public List<MapItem> findByStatus(List<StatusEnum> statusEnums, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public List<MapItem> findByStatus(List<StatusEnum> statusEnums) {
        Query query = new Query();
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        return mongoTemplate.find(query, MapItem.class);

    }

    @Override
    public List<MapItem> findByStatusAndHasNeedManual(List<StatusEnum> statusEnums, boolean hasNeedManual, Pageable pageable) {

        Query query = new Query();
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        query.addCriteria(Criteria.where("hasNeedManual").is(hasNeedManual));
        return mongoTemplate.find(query, MapItem.class);

    }

    @Override
    public List<MapItem> findByStatusAndHasNeedManual(
        String curQueryField, String searchText,
        List<StatusEnum> statusEnums, boolean hasNeedManual, List<String> clsIdList,Pageable pageable) {


        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("hasNeedManual").is(hasNeedManual));
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.find(query.with(pageable), MapItem.class);

    }

    @Override
    public List<MapItem> findByHasNeedManual(
        String curQueryField, String searchText,
        boolean hasNeedManual, List<String> clsIdList, Pageable pageable) {

        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("hasNeedManual").is(hasNeedManual));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public long countByStatus(
        String curQueryField, String searchText,
        List<StatusEnum> statusEnums,  List<String> clsIdList) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.count(query,MapItem.class);
    }

    @Override
    public long countByStatusAndHasNeedManual(
        String curQueryField, String searchText,
        List<StatusEnum> statusEnums, boolean hasNeedManual, List<String> clsIdList) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        query.addCriteria(Criteria.where("hasNeedManual").is(hasNeedManual));
        query.addCriteria(Criteria.where("processStatus").in(statusEnums));
        if (clsIdList.size() != 0)
            query.addCriteria(Criteria.where("mapCLSId").in(clsIdList));
        return mongoTemplate.count(query,MapItem.class);
    }

    @Override
    public List<MapItem> findAll(Pageable pageable) {
        Query query = new Query();
        return mongoTemplate.find(query.with(pageable), MapItem.class);
    }

    @Override
    public DeleteResult delete(MapItem mapItem) {
        return mongoTemplate.remove(mapItem);
    }
}
