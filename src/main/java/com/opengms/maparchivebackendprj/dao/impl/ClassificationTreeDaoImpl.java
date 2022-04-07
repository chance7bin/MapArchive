package com.opengms.maparchivebackendprj.dao.impl;

import com.opengms.maparchivebackendprj.dao.IClassificationTreeDao;
import com.opengms.maparchivebackendprj.entity.po.ClassificationTree;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
@Repository
public class ClassificationTreeDaoImpl implements IClassificationTreeDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ClassificationTree findByVersion(String version) {
        Query query = new Query();
        query.addCriteria(Criteria.where("version").is(version));
        return mongoTemplate.findOne(query, ClassificationTree.class);
    }

    @Override
    public ClassificationTree findById(String id) {
        return mongoTemplate.findById(id, ClassificationTree.class);
    }

    @Override
    public ClassificationTree insert(ClassificationTree classificationTree) {
        return mongoTemplate.insert(classificationTree);
    }

    @Override
    public ClassificationTree save(ClassificationTree classificationTree) {
        return mongoTemplate.save(classificationTree);
    }

}
