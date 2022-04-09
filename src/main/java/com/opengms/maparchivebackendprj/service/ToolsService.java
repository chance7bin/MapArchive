package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.CheckDTO;

public interface ToolsService {
    JsonResult statisticsMatchCount(CheckDTO checkDTO);
}
