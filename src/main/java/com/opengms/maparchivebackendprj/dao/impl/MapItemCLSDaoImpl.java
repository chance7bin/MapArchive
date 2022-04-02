package com.opengms.maparchivebackendprj.dao.impl;

import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MapItemCLSDaoImpl implements IMapItemCLSDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public MapItemCLS findById(String clsId) {
        return mongoTemplate.findById(clsId,MapItemCLS.class);
    }

    @Override
    public MapItemCLS insert(MapItemCLS mapItemCLS) {
        return mongoTemplate.insert(mapItemCLS);
    }

    @Override
    public MapItemCLS findByNameCn(String nameCn) {

        Query query = new Query();
        query.addCriteria(Criteria.where("nameCn").is(nameCn));
        return mongoTemplate.findOne(query,MapItemCLS.class);

    }

    @Override
    public MapItemCLS findByNameEn(String nameEn) {
        Query query = new Query();
        query.addCriteria(Criteria.where("nameEn").is(nameEn));
        return mongoTemplate.findOne(query,MapItemCLS.class);
    }

    @Override
    public MapItemCLS save(MapItemCLS mapItemCLS) {
        return mongoTemplate.save(mapItemCLS);
    }

    @Override
    public List<MapItemCLS> findAll() {
        return mongoTemplate.findAll(MapItemCLS.class);
    }
}
