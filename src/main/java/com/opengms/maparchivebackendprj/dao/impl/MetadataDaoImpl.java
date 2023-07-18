package com.opengms.maparchivebackendprj.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.entity.enums.MapTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/01
 */
@Slf4j
@Repository
public class MetadataDaoImpl implements IMetadataDao {

    @Autowired
    MongoTemplate mongoTemplate;


    // @Override
    // public List<JSONObject> findMetadataByOriginalNum(String filename, MapClassification mapCLS) {
    //     Query query = new Query();
    //     query.addCriteria(Criteria.where("原图幅编号").is(filename));
    //     return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());
    //
    // }
    //
    // @Override
    // public List<JSONObject> findMetadataByOriginalNumAndYear(String filename, MapClassification mapCLS) {
    //     Query query = new Query();
    //     query.addCriteria(Criteria.where("numAndYear").is(filename));
    //     return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());
    //
    // }
    //
    // @Override
    // public List<JSONObject> findMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS, Pageable pageable) {
    //     Query query = new Query();
    //     query.addCriteria(Criteria.where(curQueryField).regex(searchText));
    //     return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());
    // }
    //
    // @Override
    // public long countMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS) {
    //     Query query = new Query();
    //     query.addCriteria(Criteria.where(curQueryField).regex(searchText));
    //     return mongoTemplate.count(query, JSONObject.class, mapCLS.getNameEn());
    // }

    @Override
    public List<JSONObject> findMetadataByOriginalNum(String mapType, String matchField, String filename, String collection) {
        Query query = new Query();
        if (!mapType.equals("")){       // 按地图类型查询
            String mapFiled = MapTypeEnum.valueOf(MapTypeEnum.class,mapType).getField();
            query.addCriteria(Criteria.where(mapFiled).is(mapType));
        }
        query.addCriteria(Criteria.where(matchField).is(filename));
        return mongoTemplate.find(query, JSONObject.class, collection);
    }

    @Override
    public List<JSONObject> findMetadataByOriginalNumAndYear(String mapType, String matchFieldAndYear, String filename, String collection) {
        Query query = new Query();
        if (!mapType.equals("")){       // 按地图类型查询
            String mapFiled = MapTypeEnum.valueOf(MapTypeEnum.class,mapType).getField();
            query.addCriteria(Criteria.where(mapFiled).is(mapType));
        }
        query.addCriteria(Criteria.where(matchFieldAndYear).is(filename));
        return mongoTemplate.find(query, JSONObject.class, collection);
    }

    @Override
    public List<JSONObject> findMetadataBySearchText(String curQueryField, String searchText, String collection, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        return mongoTemplate.find(query.with(pageable), JSONObject.class, collection);
    }

    @Override
    public long countMetadataBySearchText(String curQueryField, String searchText, String collection) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        return mongoTemplate.count(query, JSONObject.class, collection);
    }

//    @Override
//    public List<JSONObject> findBSMMetadata(String clsNameCn) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("比例尺").is(clsNameCn));
//        return mongoTemplate.find(query, JSONObject.class, "BASIC_SCALE_MAP_TWENTY");
//
//    }

    @Override
    public List<JSONObject> findBSMMetadata(String collection) {
        Query query = new Query();
        return mongoTemplate.find(query, JSONObject.class, collection);

    }


}
