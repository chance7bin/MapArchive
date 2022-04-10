package com.opengms.maparchivebackendprj.controller;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.ILogService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Api(tags = "日志记录接口")
@RestController
@RequestMapping(value = "/log")
@Slf4j
public class LogController {

    @Autowired
    ILogService logService;

    @Autowired
    IGenericService genericService;


    @ApiOperation(value = "得到日志列表" )
    @PostMapping("/list")
    public JsonResult getList(@RequestBody FindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }
        return logService.getList(findDTO);
    }


    @ApiOperation(value = "根据操作类型得到日志列表" )
    @PostMapping("/list/{type}")
    public JsonResult getListByType(@RequestBody FindDTO findDTO,@PathVariable(value = "type") OperateTypeEnum operateType){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }
        return logService.getListByType(findDTO,operateType);
    }

    @ApiOperation(value = "得到日志列表" )
    @PostMapping("/count/list")
    public JsonResult countList(@RequestBody FindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }
        return logService.countList(findDTO);
    }


    @ApiOperation(value = "根据操作类型得到日志列表" )
    @PostMapping("/count/list/{type}")
    public JsonResult countListByType(@RequestBody FindDTO findDTO,@PathVariable(value = "type") OperateTypeEnum operateType){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }
        return logService.countListByType(findDTO,operateType);
    }

}
