package com.opengms.maparchivebackendprj.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
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


    @Override
    public List<JSONObject> findMetadataByOriginalNum(String filename, MapClassification mapCLS) {
        Query query = new Query();
        query.addCriteria(Criteria.where("原图幅编号").is(filename));
        return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());

    }

    @Override
    public List<JSONObject> findMetadataByOriginalNumAndYear(String filename, MapClassification mapCLS) {
        Query query = new Query();
        query.addCriteria(Criteria.where("numAndYear").is(filename));
        return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());

    }

    @Override
    public List<JSONObject> findMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        return mongoTemplate.find(query, JSONObject.class, mapCLS.getNameEn());
    }

    @Override
    public long countMetadataBySearchText(String curQueryField, String searchText, MapClassification mapCLS) {
        Query query = new Query();
        query.addCriteria(Criteria.where(curQueryField).regex(searchText));
        return mongoTemplate.count(query, JSONObject.class, mapCLS.getNameEn());
    }


}
