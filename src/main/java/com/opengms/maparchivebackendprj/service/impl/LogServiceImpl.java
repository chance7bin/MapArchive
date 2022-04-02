package com.opengms.maparchivebackendprj.service.impl;

import cn.hutool.db.PageResult;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.ILogService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
public class LogServiceImpl implements ILogService {

    @Autowired
    ILogDao logDao;

    @Autowired
    IGenericService genericService;

    @Override
    public JsonResult getList(FindDTO findDTO) {
        Pageable pageable = genericService.getPageable(findDTO);

        List<LogInfo> logInfos = logDao.findAll(pageable);
        long count = logDao.countAll();

        return ResultUtils.success(new PageableResult<>(count,logInfos));
    }

    @Override
    public JsonResult getListByType(FindDTO findDTO, OperateTypeEnum operateType) {
        Pageable pageable = genericService.getPageable(findDTO);


        List<LogInfo> logInfos = logDao.findAllByOperateType(operateType,pageable);
        long count = logDao.countAllByOperateType(operateType);

        return ResultUtils.success(new PageableResult<>(count,logInfos));
    }

    @Override
    public void insertLogInfo(String name, List<String> itemListId, OperateTypeEnum operateType) {

        logDao.insert(new LogInfo(name,itemListId,operateType,new Date()));

    }
}
