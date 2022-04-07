package com.opengms.maparchivebackendprj.dao;

import com.opengms.maparchivebackendprj.entity.po.ClassificationTree;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
public interface IClassificationTreeDao {

    ClassificationTree findByVersion(String version);

    ClassificationTree findById(String id);

    ClassificationTree insert(ClassificationTree classificationTree);

    ClassificationTree save(ClassificationTree classificationTree);
}
