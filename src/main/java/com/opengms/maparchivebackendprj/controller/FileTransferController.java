package com.opengms.maparchivebackendprj.controller;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.Chunk;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IFileTransferService;
import com.opengms.maparchivebackendprj.service.IMetadataTableService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Api(tags = "数据上传接口")
@RestController
@RequestMapping(value = "/transfer")
@Slf4j
public class FileTransferController {



    @Autowired
    IFileTransferService fileTransferService;

    @Autowired
    IMetadataTableService metadataTableService;

    /**
     * 处理文件上传POST请求
     * 将上传的文件存放到服务器内
     * @param chunk 文件块
     * @param response 响应
     * @return 上传响应状态
     */
    @ApiOperation(value = "上传文件，若是zip包则解压" )
    @PostMapping("/upload/bigFile")
    public JsonResult uploadBigFile(
        @ModelAttribute Chunk chunk,
        HttpServletResponse response
    ){

        String mapCLSId = chunk.getMapCLSId();
        MetadataTable mapCLS = metadataTableService.findById(mapCLSId);
        if (mapCLS == null){
            return ResultUtils.error("mapItem CLS 输入错误");
        }

        return fileTransferService.uploadBigFile(chunk,response);
    }


}
