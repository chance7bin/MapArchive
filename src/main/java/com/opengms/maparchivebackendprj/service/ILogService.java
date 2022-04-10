package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */

public interface ILogService {


    JsonResult getList(FindDTO findDTO);

    JsonResult getListByType(FindDTO findDTO, OperateTypeEnum operateType);


    void insertLogInfo(String name, List<String> itemListId, OperateTypeEnum operateType);

    JsonResult countList(FindDTO findDTO);

    JsonResult countListByType(FindDTO findDTO, OperateTypeEnum operateType);
}
