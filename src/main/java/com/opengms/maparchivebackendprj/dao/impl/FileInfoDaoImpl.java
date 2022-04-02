package com.opengms.maparchivebackendprj.dao.impl;

import com.opengms.maparchivebackendprj.dao.IFileInfoDao;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Repository
public class FileInfoDaoImpl implements IFileInfoDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public FileInfo insert(FileInfo fileInfo) {
        return mongoTemplate.insert(fileInfo);
    }
}
