package com.opengms.maparchivebackendprj.controller;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.GenerateImageDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.SpatialDTO;
import com.opengms.maparchivebackendprj.service.IDataServerService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import com.sun.org.apache.bcel.internal.generic.RET;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/02
 */
@Api(tags = "请求数据服务器时调用的接口")
@RestController
@RequestMapping(value = "/dataServer")
@Slf4j
public class DataServerController {

    @Autowired
    IDataServerService dataServerService;


    @ApiOperation(value = "生成缩略图" )
    @RequestMapping(value = "/thumbnailImage", method = RequestMethod.POST)
    public void generateThumbnailImage(@RequestBody GenerateImageDTO generateImageDTO) {
        dataServerService.generateThumbnailImage(generateImageDTO);

    }


    @ApiOperation(value = "生成切片" )
    @RequestMapping(value = "/TilesImage", method = RequestMethod.POST)
    public void generateTiles(@RequestBody GenerateImageDTO generateImageDTO) {
        dataServerService.generateTiles(generateImageDTO);

    }

    @ApiOperation(value = "下载" )
    @RequestMapping(value = "/download/single/{mapItemId}", method = RequestMethod.GET)
    public void download(@PathVariable String mapItemId, HttpServletResponse response) {

        dataServerService.downloadSingleItem(mapItemId,response);
    }


    @ApiOperation(value = "批量下载" )
    @RequestMapping(value = "/download/batch", method = RequestMethod.GET)
    public void batchDownload(@RequestParam(value = "mapItemIdList") List<String> mapItemIdList,HttpServletResponse response) {
        dataServerService.downloadBatchItem(mapItemIdList,response);

    }

    @ApiOperation(value = "删除" )
    @RequestMapping(value = "/delete/single/{mapItemId}", method = RequestMethod.GET)
    public JsonResult delete(@PathVariable String mapItemId) {

        return dataServerService.deleteSingleItem(mapItemId);

    }

    @ApiOperation(value = "批量删除" )
    @RequestMapping(value = "/delete/batch", method = RequestMethod.GET)
    public JsonResult batchDelete(@RequestBody List<String> mapItemIdList) {

        return dataServerService.deleteBatchItem(mapItemIdList);

    }

}
