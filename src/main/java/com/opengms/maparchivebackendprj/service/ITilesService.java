package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.dto.TilesDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
public interface ITilesService {
    void getTiandituTiles(TilesDTO tilesDTO, HttpServletResponse response);
}
