package com.opengms.maparchivebackendprj.controller;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.ExcelPathDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IMapItemCLSService;
import com.opengms.maparchivebackendprj.service.IMetadataService;
import com.opengms.maparchivebackendprj.service.IMetadataTableService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Api(tags = "元数据接口")
@RestController
@RequestMapping(value = "/metadata")
@Slf4j
public class MetadataController {


    @Autowired
    IMetadataService metadataService;

    @Autowired
    IGenericService genericService;

    @Autowired
    IMapItemCLSService mapItemCLSService;

    @Autowired
    IMetadataTableService metadataTableService;

    @ApiOperation(value = "查询元数据表 findDTO里要指定searchText是根据什么字段查找的[curQueryField必填]" )
    @RequestMapping(value = "/list/database", method = RequestMethod.POST)
    public JsonResult getMetadata(@RequestBody SpecificFindDTO findDTO){

        if (genericService.isEmptyString(findDTO.getCurQueryField())){
            return ResultUtils.error("请输入curQueryField字段");
        }

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("请填写正确的参数");
        }

        if (genericService.isEmptyString(findDTO.getMapCLSId())){
            return ResultUtils.error("请传入mapItemCLSId");
        }

        MetadataTable cls = metadataTableService.findById(findDTO.getMapCLSId());
        if (cls == null){
            return ResultUtils.error("未找到对应的元数据表，请输入正确的clsId");
        }


        return metadataService.getMetadata(findDTO,cls.getCollection());


    }


    @ApiOperation(value = "根据excel查询元数据表" )
    @RequestMapping(value = "/list/excel", method = RequestMethod.POST)
    public JsonResult getMetadataByExcel(@RequestBody ExcelPathDTO excelPathDTO) {
        if (genericService.isEmptyString(excelPathDTO.getExcelPath())){
            return ResultUtils.error("请输入excel路径");
        }
        return metadataService.getMetadataByExcel(excelPathDTO);


    }

}
