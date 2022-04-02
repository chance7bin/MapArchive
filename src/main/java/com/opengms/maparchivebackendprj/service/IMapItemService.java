package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.*;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IMapItemService {

    void process(ProcessDTO processDTO, String username, MapItemCLS mapItemCLS);


    void insert(MapItemAddDTO mapItemAddDTO, String username, MapItemCLS mapItemCLS);

    JsonResult findItemById(String id);

    JsonResult findByPolygon(SpatialDTO spatialDTO);
    JsonResult countByPolygon(SpatialDTO spatialDTO);


    JsonResult getMapItemList(SpecificFindDTO findDTO);
    JsonResult countMapItemList(SpecificFindDTO findDTO);

    JsonResult getProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult getProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult getProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult getProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult countProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult countProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult countProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);

    JsonResult countProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums);


    void batchProcess(BatchProcessDTO processDTO);

    JsonResult checkFileExist(String mapItemId);

    void downloadSingleItem(String mapItemId, HttpServletResponse response);


    void downloadBatchItem(List<String> mapItemIdList, HttpServletResponse response);

    JsonResult deleteSingleItem(String mapItemId);

    JsonResult deleteBatchItem(List<String> mapItemIdList);

    JsonResult updateItem(String id, MapItemUpdateDTO mapItemUpdateDTO);
}
