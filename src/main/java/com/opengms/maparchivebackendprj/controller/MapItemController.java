package com.opengms.maparchivebackendprj.controller;

import com.opengms.maparchivebackendprj.annotation.UserLoginToken;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.*;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.entity.po.User;
import com.opengms.maparchivebackendprj.service.*;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Api(tags = "地图条目接口")
@RestController
@RequestMapping(value = "/mapItem")
@Slf4j
public class MapItemController {

    @Autowired
    IUserService userService;


    @Autowired
    IMapItemService mapItemService;

    @Autowired
    IGenericService genericService;

    @Autowired
    ILogService logService;

    @Autowired
    IMapItemCLSService mapItemCLSService;

    @Autowired
    IMetadataTableService metadataTableService;

    @Resource(name="defaultDataServer")
    DataServer defaultDataServer;

    @UserLoginToken
    @ApiOperation(value = "根据指定的物理地址对照片进行压缩和切片(包括地图类型和元数据)", notes = "@UserLoginToken")
    @PostMapping("/process")
    public JsonResult process(@RequestBody ProcessDTO processDTO, HttpServletRequest request){

        log.info("request /mapItem/processing");

        String processingPath = processDTO.getProcessingPath();
        File file = new File(processingPath);
        if (!file.exists() && !file.isDirectory())
            return ResultUtils.error("指定的文件夹不存在");

        User user = userService.getUserByToken(request);
        if (user == null){
            return ResultUtils.error("用户验证失败");
        }

        String mapCLSId = processDTO.getMapCLSId();
        MetadataTable mapCLS = metadataTableService.findById(mapCLSId);
        if (mapCLS == null){
            return ResultUtils.error("mapItem CLS 输入错误");
        }

        try {
            String loadPath = processDTO.getProcessingPath();
            loadPath = loadPath.replace("\\", "/");
            String flag = loadPath.split(defaultDataServer.getLoadPath())[1];
        }catch (Exception e){
            return ResultUtils.error("批处理的资源所在路径需在dataServer.xml配置文件中第一个server的loadPath下");
        }


        mapItemService.process(processDTO,user.getName(),mapCLS);

        return ResultUtils.success();
    }

    @UserLoginToken
    @ApiOperation(value = "增加条目" , notes = "@UserLoginToken")
    @PostMapping("/insert")
    public JsonResult insert(@RequestBody MapItemAddDTO mapItemAddDTO, HttpServletRequest request){

        log.info("request /mapItem/insert api success");

        User user = userService.getUserByToken(request);
        if (user == null){
            return ResultUtils.error("用户验证失败");
        }

        String mapCLSId = mapItemAddDTO.getMapCLSId();
        MetadataTable mapCLS = metadataTableService.findById(mapCLSId);
        if (mapCLS == null){
            return ResultUtils.error("mapItem CLS 输入错误");
        }
        // return mapItemService.insert(mapItemAddDTO, user.getName());

        mapItemService.insert(mapItemAddDTO, user.getName(), mapCLS);
        return ResultUtils.success();
    }


    @ApiOperation(value = "根据id查询条目" )
    @GetMapping("/query/{id}")
    public JsonResult findItemById(@PathVariable(value = "id") String id){
        return mapItemService.findItemById(id);
    }


    @ApiOperation(value = "根据传入的 polygon 的 geoJson 返回范围内的所有点" )
    @RequestMapping(value = "/polygon/list", method = RequestMethod.POST)
    public JsonResult findByPolygon(@RequestBody SpatialDTO spatialDTO) {

        if (!genericService.checkFindDTOParams(spatialDTO.getFindDTO())){
            return ResultUtils.error("参数错误");
        }

        return mapItemService.findByPolygon(spatialDTO);

    }

    @ApiOperation(value = "根据传入的 polygon 的 geoJson 返回范围内的所有点" )
    @RequestMapping(value = "/polygon/count", method = RequestMethod.POST)
    public JsonResult countByPolygon(@RequestBody SpatialDTO spatialDTO) {

        if (!genericService.checkFindDTOParams(spatialDTO.getFindDTO())){
            return ResultUtils.error("参数错误");
        }

        return mapItemService.countByPolygon(spatialDTO);

    }



    @ApiOperation(value = "分页获取条目详细信息,根据地图类型" )
    @PostMapping("/list/content")
    public JsonResult getItemList(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        return mapItemService.getMapItemList(findDTO);
    }



    @ApiOperation(value = "根据地图类型得到数据量" )
    @PostMapping("/list/count")
    public JsonResult countItemList(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        return mapItemService.countMapItemList(findDTO);
    }


    @ApiOperation(value = "得到正在进行的处理列表" )
    @RequestMapping(value = "/process/processing/content", method = RequestMethod.POST)
    public JsonResult getProcessingListStatusIsProcessing(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Inited,StatusEnum.Started);

        return mapItemService.getProcessingListStatusIsProcessing(findDTO, statusEnums);

    }

    @ApiOperation(value = "得到已完成的处理列表" )
    @RequestMapping(value = "/process/finished/content", method = RequestMethod.POST)
    public JsonResult getProcessingListStatusIsFinished(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Finished);
        return mapItemService.getProcessingListStatusIsFinished(findDTO, statusEnums);

    }

    @ApiOperation(value = "得到需要人工处理的列表" )
    @RequestMapping(value = "/process/manual/content", method = RequestMethod.POST)
    public JsonResult getProcessingListNeedManual(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Finished);
        return mapItemService.getProcessingListNeedManual(findDTO,statusEnums);

    }

    @ApiOperation(value = "得到失败的处理列表" )
    @RequestMapping(value = "/process/error/content", method = RequestMethod.POST)
    public JsonResult getProcessingListStatusIsError(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Error);
        return mapItemService.getProcessingListStatusIsError(findDTO, statusEnums);

    }

    @ApiOperation(value = "得到正在进行的处理列表数量" )
    @RequestMapping(value = "/process/processing/count", method = RequestMethod.POST)
    public JsonResult countProcessingListStatusIsProcessing(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Inited,StatusEnum.Started);

        return mapItemService.countProcessingListStatusIsProcessing(findDTO, statusEnums);

    }

    @ApiOperation(value = "得到已完成的处理列表数量" )
    @RequestMapping(value = "/process/finished/count", method = RequestMethod.POST)
    public JsonResult countProcessingListStatusIsFinished(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Finished);
        return mapItemService.countProcessingListStatusIsFinished(findDTO, statusEnums);

    }

    @ApiOperation(value = "得到需要人工处理的列表数量" )
    @RequestMapping(value = "/process/manual/count", method = RequestMethod.POST)
    public JsonResult countProcessingListNeedManual(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Finished);
        return mapItemService.countProcessingListNeedManual(findDTO,statusEnums);

    }

    @ApiOperation(value = "得到失败的处理列表数量" )
    @RequestMapping(value = "/process/error/count", method = RequestMethod.POST)
    public JsonResult countProcessingListStatusIsError(@RequestBody SpecificFindDTO findDTO){

        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        List<StatusEnum> statusEnums = Arrays.asList(StatusEnum.Error);
        return mapItemService.countProcessingListStatusIsError(findDTO, statusEnums);

    }

    @UserLoginToken
    @ApiOperation(value = "批量处理图片 默认按照创建时间倒序排序" , notes = "@UserLoginToken")
    @RequestMapping(value = "/process/batch", method = RequestMethod.POST)
    public JsonResult batchProcess(@RequestBody BatchProcessDTO processDTO, HttpServletRequest request){


        User user = userService.getUserByToken(request);
        if (user == null){
            return ResultUtils.error("用户验证失败");
        }

        mapItemService.batchProcess(processDTO);

        return ResultUtils.success();

    }

    @ApiOperation(value = "检查待下载的文件是否存在" )
    @GetMapping("/file/check/{mapItemId}")
    public JsonResult checkFileExist(@PathVariable String mapItemId){
        return mapItemService.checkFileExist(mapItemId);
    }


    @UserLoginToken
    @ApiOperation(value = "删除单文件", notes = "@UserLoginToken" )
    @GetMapping("/delete/single/{mapItemId}")
    public JsonResult deleteSingleItem(@PathVariable String mapItemId,HttpServletRequest request){
        User user = userService.getUserByToken(request);
        logService.insertLogInfo(user.getName(),Arrays.asList(mapItemId), OperateTypeEnum.DELETE);
        return mapItemService.deleteSingleItem(mapItemId);
    }

    @UserLoginToken
    @ApiOperation(value = "批量删除文件 传过来的条目id列表" , notes = "@UserLoginToken")
    @PostMapping("/delete/batch")
    public JsonResult deleteBatchItem(@RequestBody List<String> mapItemIdList, HttpServletRequest request){

        User user = userService.getUserByToken(request);
        // List<String> list = Arrays.asList(mapItemIdList);
        logService.insertLogInfo(user.getName(),mapItemIdList, OperateTypeEnum.DELETE);
        return mapItemService.deleteBatchItem(mapItemIdList);
    }


    @UserLoginToken
    @ApiOperation(value = "下载单个条目", notes = "@UserLoginToken" )
    @GetMapping("/download/single/{mapItemId}")
    public void downloadSingleItem(@PathVariable String mapItemId, HttpServletRequest request, HttpServletResponse response){
        User user = userService.getUserByToken(request);
        logService.insertLogInfo(user.getName(),Arrays.asList(mapItemId), OperateTypeEnum.DOWNLOAD);
        mapItemService.downloadSingleItem(mapItemId,response);
    }


    @UserLoginToken
    @ApiOperation(value = "批量下载条目 传过来条目id列表", notes = "@UserLoginToken" )
    @PostMapping("/download/batch")
    public void downloadBatchItem(@RequestBody List<String> mapItemIdList,HttpServletRequest request,HttpServletResponse response){
        User user = userService.getUserByToken(request);
        logService.insertLogInfo(user.getName(),mapItemIdList, OperateTypeEnum.DOWNLOAD);
        mapItemService.downloadBatchItem(mapItemIdList,response);
    }


    @UserLoginToken
    @ApiOperation(value = "修改条目信息(目前只能修改元数据)" , notes = "@UserLoginToken")
    @PostMapping("/update/metadata/{id}")
    public JsonResult updateItemMetadata(
        @ApiParam(name = "id", value = "传的是mapItem里的id") @PathVariable String id, @RequestBody MapItemUpdateDTO mapItemUpdateDTO, HttpServletRequest request){

        User user = userService.getUserByToken(request);
        logService.insertLogInfo(user.getName(),Arrays.asList(id), OperateTypeEnum.UPDATE);
        return mapItemService.updateItem(id, mapItemUpdateDTO);
    }


}
