package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.GenerateImageDTO;
import com.opengms.maparchivebackendprj.service.IDataServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/02
 */
@Service
@Slf4j
public class DataServerImpl implements IDataServerService {


    @Override
    public void generateThumbnailImage(GenerateImageDTO generateImageDTO) {

    }

    @Override
    public void generateTiles(GenerateImageDTO generateImageDTO) {

    }

    @Override
    public void downloadSingleItem(String mapItemId, HttpServletResponse response) {

    }

    @Override
    public void downloadBatchItem(List<String> mapItemIdList, HttpServletResponse response) {

    }

    @Override
    public JsonResult deleteSingleItem(String mapItemId) {
        return null;
    }

    @Override
    public JsonResult deleteBatchItem(List<String> mapItemIdList) {
        return null;
    }
}
