package com.opengms.maparchivebackendprj.service;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.dto.TiandituTilesDTO;
import com.opengms.maparchivebackendprj.entity.dto.TilesDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
public interface ITilesService {
    void getTiandituTiles(TiandituTilesDTO tilesDTO, HttpServletResponse response);

    void getMapboxTiles(TilesDTO tilesDTO, HttpServletResponse response);

    JSONObject getMapboxTilesMetadataJson();
}
