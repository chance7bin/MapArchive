package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/28
 */
public interface IMapItemCLSService {


    MapItemCLS findById(String mapCLSId);
}
