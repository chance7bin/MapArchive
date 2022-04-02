package com.opengms.maparchivebackendprj.dao.impl;

import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
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
 * @Date 2022/03/24
 */
@Repository
public class LogDaoImpl implements ILogDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public LogInfo insert(LogInfo logInfo) {

        return mongoTemplate.insert(logInfo);

    }

    @Override
    public List<LogInfo> findAll(Pageable pageable) {
        Query query = new Query();
        return mongoTemplate.find(query.with(pageable),LogInfo.class);
    }

    @Override
    public long countAll() {
        return mongoTemplate.estimatedCount(LogInfo.class);
    }

    @Override
    public List<LogInfo> findAllByOperateType(OperateTypeEnum operateType, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("operateType").is(operateType));
        return mongoTemplate.find(query.with(pageable),LogInfo.class);
    }

    @Override
    public long countAllByOperateType(OperateTypeEnum operateType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("operateType").is(operateType));
        return mongoTemplate.count(query,LogInfo.class);
    }
}
