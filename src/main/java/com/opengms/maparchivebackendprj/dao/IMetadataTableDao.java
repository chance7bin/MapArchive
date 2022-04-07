package com.opengms.maparchivebackendprj.dao;

import com.opengms.maparchivebackendprj.entity.po.MetadataTable;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
public interface IMetadataTableDao {

    MetadataTable findByName(String name);

    MetadataTable findByCollection(String collection);

    MetadataTable findById(String id);

    MetadataTable insert(MetadataTable metadataTable);

    List<MetadataTable> findAll();

}
