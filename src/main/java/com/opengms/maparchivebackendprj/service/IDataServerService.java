package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.GenerateImageDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/02
 */
public interface IDataServerService {


    void generateThumbnailImage(GenerateImageDTO generateImageDTO);

    void generateTiles(GenerateImageDTO generateImageDTO);

    void downloadSingleItem(String mapItemId, HttpServletResponse response);

    void downloadBatchItem(List<String> mapItemIdList, HttpServletResponse response);

    JsonResult deleteSingleItem(String mapItemId);

    JsonResult deleteBatchItem(List<String> mapItemIdList);
}
