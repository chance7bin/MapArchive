package com.opengms.maparchivebackendprj.service.impl;

import cn.hutool.db.PageResult;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.ILogService;
import com.opengms.maparchivebackendprj.service.IMapItemService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    IMapItemDao mapItemDao;

    @Override
    public JsonResult getList(FindDTO findDTO) {
        Pageable pageable = genericService.getPageable(findDTO);

        List<LogInfo> logInfos = logDao.findAll(pageable);


        // long count = logDao.countAll();

        // return ResultUtils.success(new PageableResult<>(count,logInfos));
        return ResultUtils.success(formatLogInfo(logInfos));
    }

    //LogInfo中的itemListId需附上对应的条目名字用于展示
    private List<JSONObject> formatLogInfo(List<LogInfo> logInfos){

        List<JSONObject> result = new ArrayList<>();
        for (LogInfo logInfo : logInfos) {
            JSONObject jsonObject = new JSONObject();

            List<JSONObject> items = new ArrayList<>();
            List<String> itemListId = logInfo.getItemListId();
            if (!(itemListId == null || itemListId.size() == 0)){
                for (String itemId : itemListId) {
                    JSONObject jsonObject1 = new JSONObject();
                    MapItem mapItem = mapItemDao.findById(itemId);
                    String name = mapItem != null ? mapItem.getName() : "未知图幅(可能已被删除)";
                    jsonObject1.put("id",itemId);
                    jsonObject1.put("name",name);
                    items.add(jsonObject1);
                }
            }

            jsonObject.put("operator",logInfo.getOperator());
            jsonObject.put("operateType",logInfo.getOperateType());
            jsonObject.put("createTime",logInfo.getCreateTime());
            jsonObject.put("itemList",items);

            result.add(jsonObject);
        }

        return result;

    }

    @Override
    public JsonResult getListByType(FindDTO findDTO, OperateTypeEnum operateType) {
        Pageable pageable = genericService.getPageable(findDTO);


        List<LogInfo> logInfos = logDao.findAllByOperateType(operateType,pageable);
        // long count = logDao.countAllByOperateType(operateType);

        // return ResultUtils.success(new PageableResult<>(count,logInfos));
        return ResultUtils.success(formatLogInfo(logInfos));
    }

    @Override
    public void insertLogInfo(String name, List<String> itemListId, OperateTypeEnum operateType) {

        logDao.insert(new LogInfo(name,itemListId,operateType,new Date()));

    }

    @Override
    public JsonResult countList(FindDTO findDTO) {
        return ResultUtils.success(logDao.countAll());
    }

    @Override
    public JsonResult countListByType(FindDTO findDTO, OperateTypeEnum operateType) {
        return ResultUtils.success(logDao.countAllByOperateType(operateType));
    }
}
