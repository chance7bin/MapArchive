package com.opengms.maparchivebackendprj.dao;

import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IMapItemCLSDao {
    MapItemCLS findById(String clsId);

    MapItemCLS insert(MapItemCLS mapItemCLS);

    MapItemCLS findByNameCn(String nameCn);

    MapItemCLS findByNameEn(String nameEn);

    MapItemCLS save(MapItemCLS mapItemCLS);

    List<MapItemCLS> findAll();
}
