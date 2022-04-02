package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import com.opengms.maparchivebackendprj.service.IMapItemCLSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/28
 */
@Service
public class MapItemCLSImpl implements IMapItemCLSService {

    @Autowired
    IMapItemCLSDao mapItemCLSDao;


    @Override
    public MapItemCLS findById(String mapCLSId) {
        return mapItemCLSDao.findById(mapCLSId);
    }
}
