package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.Chunk;
import com.opengms.maparchivebackendprj.entity.dto.MapChunk;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IFileTransferService {

    JsonResult uploadMapFile(MapChunk chunk, HttpServletResponse response);


    JsonResult uploadFile(Chunk chunk, HttpServletResponse response);
}
