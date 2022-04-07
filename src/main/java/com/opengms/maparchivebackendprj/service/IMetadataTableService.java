package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.po.MetadataTable;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
public interface IMetadataTableService {

    MetadataTable findById(String id);

}
