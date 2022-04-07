package com.opengms.maparchivebackendprj.dao.impl;

import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.entity.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
@Repository
public class MetadataTableDaoImpl implements IMetadataTableDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public MetadataTable findByName(String name) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(name));

        return mongoTemplate.findOne(query, MetadataTable.class);
    }

    @Override
    public MetadataTable findByCollection(String collection) {
        Query query = new Query();
        query.addCriteria(Criteria.where("collection").is(collection));

        return mongoTemplate.findOne(query, MetadataTable.class);
    }

    @Override
    public MetadataTable findById(String id) {
        return mongoTemplate.findById(id, MetadataTable.class);
    }

    @Override
    public MetadataTable insert(MetadataTable metadataTable) {
        return mongoTemplate.insert(metadataTable);
    }

    @Override
    public List<MetadataTable> findAll() {
        return mongoTemplate.findAll(MetadataTable.class);
    }
}
