package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IMetadataTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
@Service
public class MetadataTableServiceImpl implements IMetadataTableService {

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Override
    public MetadataTable findById(String id) {
        return metadataTableDao.findById(id);
    }
}
