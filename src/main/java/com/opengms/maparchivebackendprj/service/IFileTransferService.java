package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.Chunk;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IFileTransferService {

    JsonResult uploadBigFile(Chunk chunk, HttpServletResponse response);


}
