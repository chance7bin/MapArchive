package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IClassificationTreeDao;
import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ImageUrl;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ProcessParam;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ScaleCoordinate;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.*;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.*;
import com.opengms.maparchivebackendprj.service.*;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ImageUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPolygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
@Slf4j
public class MapItemServiceImpl implements IMapItemService {

    @Value("${resourcePath}")
    private String resourcePath;

    // @Value("${dataServerPath}")
    // private String dataServerPath;

    // @Resource(name="defaultDataServer")
    // DataServer defaultDataServer;

    @Value("${mapItemDir}")
    private String mapItemDir;

    @Autowired
    IMapItemDao mapItemDao;

    @Autowired
    ILogDao logDao;

    @Autowired
    IMetadataService metadataService;

    @Autowired
    IGeoInfoService geoInfoService;

    @Autowired
    IAsyncService asyncService;

    @Autowired
    IGenericService genericService;

    @Autowired
    IMapItemCLSDao mapItemCLSDao;

    @Autowired
    IClassificationTreeDao classificationTreeDao;


    @Resource(name="defaultDataServer")
    DataServer defaultDataServer;



    // 异步调用处理任务，防止请求阻塞
    @Async
    @Override
    public void process(ProcessDTO processDTO, String username, MetadataTable mapItemCLS) {

        log.info("process invoke begin [ {} ]", new Date());

        String processingPath = processDTO.getProcessingPath();
//        String savePath = processDTO.getSavePath();

        //把路径中的 \ 都替换为 /
        processingPath = processingPath.replace("\\", "/");
//        savePath = savePath.replace("\\", "/");

        // 自动生成保存路径
        String cls = mapItemCLS.getCollection();
        // String savePath = resourcePath +  "/" + cls;
        String savePath = genericService.getLoadPath(processDTO.getServername()) + mapItemDir +  "/" + cls;

        // MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls);

        File file = new File(processingPath);

        File[] files = file.listFiles();

        if (files == null){
            return;
        }

        // 得到所指文件列表
        List<File> fileList = new ArrayList<>();
        for (File f : files) {
            fileList.addAll(getFileList(f));
        }

        String mapType = processDTO.getMapType();
        List<String> itemListId = new ArrayList<>();
        for (File file1 : fileList) {
            String name = file1.getName();
            long itemCount = mapItemDao.countByName(name, processDTO.getMapCLSId(), mapType);
            // 按元数据结合地图类型(如基本比例尺中的地图种类：协同图、地形图...)查询
            if(itemCount != 0 && !mapType.equals("航空图") && !mapType.equals("联合作战图")){
//                 但基本比例尺的航空图，联合作战图除外, 因为和地形图都属于基本比例尺地图，可能有地形图匹配了航空图或联合作战图
//                if(mapType != "航空图" && mapType != "联合作战图"){
                    return;
//                }
            }
            //创建实体
            MapItem mapItem = new MapItem();

            mapItem.setName(file1.getName());
            mapItem.setAuthor(username);
            mapItem.setMapCLSId(processDTO.getMapCLSId());
            mapItem.setMapType(mapType);
            mapItemDao.insert(mapItem);

            itemListId.add(mapItem.getId());

            asyncService.initMapItem(mapItem,file1.getPath(),savePath,processingPath, processDTO ,null);

        }

        logDao.insert(new LogInfo(username,itemListId, OperateTypeEnum.MOUNT,new Date()));


    }

    @Async
    @Override
    public void insert(MapItemAddDTO mapItemAddDTO, String username, MetadataTable mapItemCLS) {

        String loadPath = genericService.getLoadPath(mapItemAddDTO.getServername());

        // String processingPath = resourcePath +  "/" + mapItemCLS.getNameEn() + "/file";
        String processingPath = loadPath + mapItemDir +  "/" + mapItemCLS.getCollection() + "/file";
        // String savePath = resourcePath +  "/" + mapItemCLS.getNameEn();
        String savePath = loadPath + mapItemDir +  "/" + mapItemCLS.getCollection();


        // String cls = mapItemCLS.getNameEn();

        // MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls);

        // List<MapItem> mapItemList = new ArrayList<>();
        List<String> itemListId = new ArrayList<>();
        List<FileInfo> fileInfoList = mapItemAddDTO.getFileInfoList();
        String mapType = mapItemAddDTO.getMapType();
        if (fileInfoList != null){
            for (FileInfo fileInfo : fileInfoList) {
                String name = fileInfo.getFileName();
                long itemCount = mapItemDao.countByName(name, mapItemAddDTO.getMapCLSId(), mapType);
                // 查询名字，重复的不上传
                if(itemCount != 0){
                    // 但基本比例尺的航空图，联合作战图除外, 因为和地形图都属于基本比例尺地图，可能有地形图匹配了航空图或联合作战图
                    if(mapType != "航空图" && mapType != "联合作战图"){
                        return;
                    }
                }
                // MapItem mapItem = new MapItem();
                MapItem mapItem = new MapItem();
                mapItem.setAuthor(username);
                mapItem.setName(fileInfo.getFileName());
                mapItem.setMapCLSId(mapItemAddDTO.getMapCLSId());
                mapItem.setMapType(mapType);
                mapItemDao.insert(mapItem);

                itemListId.add(mapItem.getId());

                ProcessDTO processDTO = new ProcessDTO(
                    processingPath,
                    mapItemCLS.getId(),
                    mapItemAddDTO.getMapType(),
                    mapItemAddDTO.getMetadataTable(),
                    mapItemAddDTO.isMatchMetadata(),
                    mapItemAddDTO.isCalcGeoInfo(),
                    mapItemAddDTO.isGenerateThumbnail(),
                    mapItemAddDTO.isGenerateTiles(),
                    mapItemAddDTO.getServername()
                    );

                // mapItemList.add(initMapItem(mapItem,fileInfo.getPath(),savePath,processingPath, processDTO, fileInfo));
                asyncService.initMapItem(mapItem,fileInfo.getPath(),savePath,processingPath, processDTO, fileInfo);
            }
        }

        logDao.insert(new LogInfo(username,itemListId, OperateTypeEnum.UPLOAD,new Date()));


        // return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult findItemById(String id) {

        MapItem item = mapItemDao.findById(id);
        if (item == null)
            return ResultUtils.error("we can't find item by this id");
        return ResultUtils.success(item);


    }

    @Override
    public JsonResult findByPolygon(SpatialDTO spatialDTO) {

        List<List<Double>> pointList = spatialDTO.getPointList();
        SpecificFindDTO findDTO = spatialDTO.getFindDTO();

        Pageable pageable = genericService.getPageable(findDTO);

        // // 用polygon查
        // GeoJsonPolygon polygon = new GeoJsonPolygon(
        //     new Point(pointList.get(0).get(0),pointList.get(0).get(1)),
        //     new Point(pointList.get(1).get(0),pointList.get(1).get(1)),
        //     new Point(pointList.get(2).get(0),pointList.get(2).get(1)),
        //     new Point(pointList.get(3).get(0),pointList.get(3).get(1)),
        //     new Point(pointList.get(0).get(0),pointList.get(0).get(1))
        // );
        //
        // //用box查
        // Box box = new Box(
        //     new Point(pointList.get(0).get(0),pointList.get(0).get(1)),
        //     new Point(pointList.get(2).get(0),pointList.get(2).get(1))
        // );

        Double leftLon = pointList.get(0).get(0);
        Double rightLon = pointList.get(2).get(0);
        Double bottomLat = pointList.get(0).get(1);
        Double upperLat = pointList.get(2).get(1);

        // Double middleLon = 0.0;
        //
        // if (rightLon - leftLon > 180){
        //     middleLon = (rightLon + leftLon) / 2;
        // }
        //
        //
        // GeoJsonMultiPolygon multiPolygon = new GeoJsonMultiPolygon(
        //     Arrays.asList(
        //         new GeoJsonPolygon(new Point(leftLon,bottomLat),new Point(leftLon,upperLat),
        //             new Point(middleLon,upperLat),new Point(middleLon,bottomLat),new Point(leftLon,bottomLat)),
        //         new GeoJsonPolygon(
        //             new Point(middleLon,bottomLat),new Point(middleLon,upperLat),
        //             new Point(rightLon,upperLat),new Point(rightLon,bottomLat),new Point(middleLon,bottomLat))
        //     )
        // );

        String curQueryField = findDTO.getCurQueryField();
        if (curQueryField == null || curQueryField.equals("")){
            curQueryField = "name";
        }


        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());


        List<MapItem> mapItemList = null;
        List<GeoJsonPolygon> queryPolygon = getQueryPolygon(leftLon, rightLon, bottomLat, upperLat);

        if (queryPolygon.size() == 1){
            mapItemList = mapItemDao.findBySearchTextAndPolygonAndPageable(
                curQueryField, findDTO.getSearchText(), queryPolygon.get(0), mapClassifications, pageable);
        } else {
            mapItemList = mapItemDao.findBySearchTextAndPolygonAndPageable(
                curQueryField, findDTO.getSearchText(), new GeoJsonMultiPolygon(queryPolygon), mapClassifications, pageable);
        }




        // PageableResult<MapItem> items = findBySearchTextAndPolygonAndPageable
        //     (curQueryField, findDTO.getSearchText(), polygon, findDTO.getMapCLSId(), pageable);


        return ResultUtils.success(mapItemList);
    }

    //得到查询的多边形范围
    private List<GeoJsonPolygon> getQueryPolygon(double leftLon, double rightLon,double bottomLat,double upperLat){
        // Double leftLon = pointList.get(0).get(0);
        // Double rightLon = pointList.get(2).get(0);
        // Double bottomLat = pointList.get(0).get(1);
        // Double upperLat = pointList.get(2).get(1);
        // Double middleLon = 0.0;

        List<GeoJsonPolygon> polygons = new ArrayList<>();

        // 1.先把框选范围移到左边经度大于-180的情况
        while (leftLon < -180){
            leftLon += 360;
            rightLon += 360;
        }


        // 2.如果右边经度此时小于180的话
        // 接着判断经度跨度是否小于180，
        // 如果小于的话直接通过GeoJsonPolygon查找
        // 如果大于的话就把box从中间切开，通过GeoJsonMultiPolygon进行查找
        if (rightLon < 180){
            polygons.addAll(getQueryPolygon_standard(leftLon,rightLon,bottomLat,upperLat));
        }

        // 3.如果右边经度此时大于等于180的话
        // 就要把 >=180 的范围从180°经线切开，形成两个box进行查找
        // 分别是
        // (leftLon, 179.9)
        // (-179.9, rightLon-360)
        // 由于这两个box肯定是符合第二个情况的，所以接下来这两个box分别进行第二步的讨论就行
        else {
            polygons.addAll(getQueryPolygon_standard(leftLon,179.9,bottomLat,upperLat));
            polygons.addAll(getQueryPolygon_standard(-179.9,rightLon-360,bottomLat,upperLat));
        }


        return polygons;

    }


    //上面的第二个步骤是要多次调用的，单独抽出来
    private List<GeoJsonPolygon> getQueryPolygon_standard(Double leftLon, Double rightLon,Double bottomLat,Double upperLat){

        List<GeoJsonPolygon> polygons = new ArrayList<>();

        if (rightLon - leftLon < 180){
            polygons.add(new GeoJsonPolygon(
                new Point(leftLon,bottomLat),new Point(leftLon,upperLat),
                new Point(rightLon,upperLat),new Point(rightLon,bottomLat),
                new Point(leftLon,bottomLat)));
        }else {
            double middleLon = (rightLon + leftLon) / 2;
            polygons.add(new GeoJsonPolygon(new Point(leftLon,bottomLat),new Point(leftLon,upperLat),
                new Point(middleLon,upperLat),new Point(middleLon,bottomLat),new Point(leftLon,bottomLat)));
            polygons.add(new GeoJsonPolygon(
                new Point(middleLon,bottomLat),new Point(middleLon,upperLat),
                new Point(rightLon,upperLat),new Point(rightLon,bottomLat),new Point(middleLon,bottomLat)));
        }

        return polygons;
    }


    @Override
    public JsonResult countByPolygon(SpatialDTO spatialDTO) {

        List<List<Double>> pointList = spatialDTO.getPointList();
        SpecificFindDTO findDTO = spatialDTO.getFindDTO();

        Double leftLon = pointList.get(0).get(0);
        Double rightLon = pointList.get(2).get(0);
        Double bottomLat = pointList.get(0).get(1);
        Double upperLat = pointList.get(2).get(1);


        String curQueryField = findDTO.getCurQueryField();
        if (curQueryField == null || curQueryField.equals("")){
            curQueryField = "name";
        }

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long l;
        List<GeoJsonPolygon> queryPolygon = getQueryPolygon(leftLon, rightLon, bottomLat, upperLat);
        if (queryPolygon.size() == 1){
            l = mapItemDao.countBySearchTextAndPolygon(
                curQueryField, findDTO.getSearchText(), queryPolygon.get(0), mapClassifications);
        } else {
            l = mapItemDao.countBySearchTextAndPolygon(
                curQueryField, findDTO.getSearchText(), new GeoJsonMultiPolygon(queryPolygon), mapClassifications);
        }

        return ResultUtils.success(l);

    }

    @Override
    public JsonResult getMapItemList(SpecificFindDTO findDTO) {

        Pageable pageable = genericService.getPageable(findDTO);

        String searchText = findDTO.getSearchText();
        String curQueryField = findDTO.getCurQueryField();

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());


        List<MapItem> mapItemList =
            mapItemDao.findBySearchTextAndPageable(curQueryField, searchText, mapClassifications, pageable);


        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult countMapItemList(SpecificFindDTO findDTO) {

        String searchText = findDTO.getSearchText();
        String curQueryField = findDTO.getCurQueryField();

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());


        long count =
            mapItemDao.countBySearchText(curQueryField, searchText, mapClassifications);


        return ResultUtils.success(count);


    }


    private PageableResult<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, String mapCLSId, Pageable pageable){

        List<String> mapClassifications = buildClassifications(mapCLSId);

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndPolygonAndPageable(curQueryField,searchText,polygon,mapClassifications,pageable);

        long count = mapItemDao.countBySearchTextAndPolygon(curQueryField, searchText, polygon, mapClassifications);

        return new PageableResult<>(count,mapItemList);

    }


    /**
     * 构建classifications集合,如果没有childrenId则表示只查询一个分类条目，有的话表示有多个分类条目
     * @param clsId
     * @Author bin
     **/
    public List<String> buildClassifications(String clsId){
        ClassificationTree basic = classificationTreeDao.findByVersion("basic");
        return buildClassifications(clsId, basic);
    }
    public Map<String,Boolean> getItemProcessList(BatchProcessDTO processDTO){
        Map<String,Boolean> processList = new HashMap<>();
        if(processDTO.isGenerateThumbnail()){
            processList.put("Thumbnail",true);
        }
        if(processDTO.isGenerateTiles()){
            processList.put("Tiles",true);
        }
        if(processDTO.isCalcGeoInfo()){
            processList.put("GeoInfo",true);
        }
        if(processDTO.isMatchMetadata()){
            processList.put("matchMetadata",true);
        }
        return processList;
    }

    @Override
    public JsonResult updateGeoInfo(String id, MapItemUpdateDTO mapItemUpdateDTO) {

        MapItem mapItem = mapItemDao.findById(id);
        if (mapItem == null)
            return ResultUtils.error("no item");

        List<List<Double>> pointList = mapItemUpdateDTO.getPointList();

        Double leftLon = pointList.get(0).get(0);
        Double rightLon = pointList.get(2).get(0);
        Double bottomLat = pointList.get(0).get(1);
        Double upperLat = pointList.get(2).get(1);

        ScaleCoordinate coordinate = new ScaleCoordinate(leftLon, bottomLat, rightLon, upperLat);

        GeoInfo geoInfo = geoInfoService.getGeoInfo(coordinate);

        if (geoInfo != null){
            mapItem.setCenter(geoInfo.getCenter());
            mapItem.setPolygon(geoInfo.getPolygon());
            mapItem.setHasCalcCoordinate(true);
            // mapItem.setHasNeedManual(false);
        } else {
            mapItem.setHasCalcCoordinate(false);
            // mapItem.setHasNeedManual(true);
        }

        if (genericService.hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        MapItem save = mapItemDao.save(mapItem);

        return ResultUtils.success(save);

    }

    @Override
    public JsonResult generateThumbnail(String id) {

        MapItem mapItem = mapItemDao.findById(id);
        if (mapItem == null)
            return ResultUtils.error("未找到该条目");

        if (mapItem.getProcessStatus() != StatusEnum.Finished)
            return ResultUtils.error("该条目正在处理中，请稍后");

        if (mapItem.getThumbnailStatus() == StatusEnum.Finished)
            return ResultUtils.error("已生成缩略图");

        mapItem.setProcessStatus(StatusEnum.Inited);
        mapItemDao.save(mapItem);

        asyncService.generateThumbnail(id);

        return ResultUtils.success();

    }

    @Override
    public JsonResult generateTiles(String id) {
        MapItem mapItem = mapItemDao.findById(id);
        if (mapItem == null)
            return ResultUtils.error("未找到该条目");

        if (mapItem.getProcessStatus() != StatusEnum.Finished)
            return ResultUtils.error("该条目正在处理中，请稍后");

        if (mapItem.getTileStatus() == StatusEnum.Finished)
            return ResultUtils.error("已生成瓦片");

        mapItem.setProcessStatus(StatusEnum.Inited);
        mapItemDao.save(mapItem);

        asyncService.generateTiles(id);

        return ResultUtils.success();
    }

    private List<String> buildClassifications(String clsId, ClassificationTree classificationTree){
        List<String> classifications = new ArrayList<>();

        // 判断是否传入clsId
        if (genericService.isEmptyString(clsId))
            return classifications;

        JSONObject cls = findTreeItemById(clsId, classificationTree.getTree());
        //判断该cls是否存在
        if (!genericService.isExist(cls))
            return classifications;

        JSONArray children = cls.getJSONArray("children");
        if (children == null || children.size() == 0){
            // if (cls.getNameEn() != null){
            //     MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls.getNameEn());
            //     if (mapCLSByNameEn != null){
            //         classifications.add(mapCLSByNameEn.getNameEn());
            //     }
            // }
            classifications.add(cls.getString("id"));
        }
        else {
            // for (String child : childrenId) {
            //     classifications.addAll(buildClassifications(child));
            // }
            for (int i = 0; i < children.size(); i++) {
                JSONObject child = children.getJSONObject(i);
                classifications.addAll(buildClassifications(child.getString("id"),classificationTree));
            }
        }
        return classifications;
    }

    private JSONObject findTreeItemById(String id, JSONArray tree){

        JSONObject result = null;
        for (int i = 0; i < tree.size(); i++) {

            JSONObject item = tree.getJSONObject(i);
            if (item.getString("id").equals(id)){
                result = item;
                break;
            }
            JSONArray children = item.getJSONArray("children");
            if (children != null && children.size() != 0){
                result = findTreeItemById(id,children);
                if (result != null){
                    break;
                }
            }

        }

        return result;

    }

    //得到所指文件列表
    private List<File> getFileList(File file){
        // List<File> fileList;
        List<File> fileList = new ArrayList<>();
        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                fileList.addAll(getFileList(f));
            }
        }
        else {
            fileList.add(file);
        }
        return fileList;

    }


    @Override
    public JsonResult getProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult getProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, false, true, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);


    }

    @Override
    public JsonResult getProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true, true, mapClassifications,pageable);
        return ResultUtils.success(mapItemList);
    }

    @Override
    public JsonResult getProcessingListNeedMatch(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true, false, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult getProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        Pageable pageable = genericService.getPageable(findDTO);

        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);
    }

    @Override
    public JsonResult countProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, false, true, mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true, true, mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListNeedMatch(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true, false, mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<String> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public void batchProcess(BatchProcessDTO processDTO,String username) {

        FindDTO findDTO = new FindDTO(1,processDTO.getProcessCount(),false,"createTime");

        Pageable pageable = genericService.getPageable(findDTO);

        Map<String,Boolean> batchList = getItemProcessList(processDTO);

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(Arrays.asList(StatusEnum.Finished), batchList, processDTO.getMapCLSId(), true, pageable);

        List<String> itemListId = new ArrayList<>();

        for (MapItem mapItem : mapItemList) {
            mapItem.setProcessStatus(StatusEnum.Inited);
            mapItemDao.save(mapItem);

            itemListId.add(mapItem.getId());

            asyncService.batchProcess(mapItem,processDTO);
        }

        logDao.insert(new LogInfo(username,itemListId, OperateTypeEnum.PROCESS,new Date()));

    }

    @Override
    public void matchErrorProcess(BatchProcessDTO processDTO, String username) {

        FindDTO findDTO = new FindDTO(1,processDTO.getProcessCount(),false,"createTime");

        Pageable pageable = genericService.getPageable(findDTO);

        List<MapItem> mapItemList = mapItemDao.findByHasNeedMatch(Arrays.asList(StatusEnum.Finished), processDTO.getMapCLSId(),true,false,pageable);

        List<String> itemListId = new ArrayList<>();

        for (MapItem mapItem : mapItemList) {
            mapItem.setProcessStatus(StatusEnum.Inited);
            mapItemDao.save(mapItem);

            itemListId.add(mapItem.getId());

            asyncService.batchProcess(mapItem,processDTO);
        }

        logDao.insert(new LogInfo(username,itemListId, OperateTypeEnum.PROCESS,new Date()));

    }

    @Override
    public JsonResult checkFileExist(String mapItemId) {
        MapItem mapItem = mapItemDao.findById(mapItemId);
        if (mapItem == null){
            log.warn("can not find item by this id");
            return ResultUtils.error("can not find item by this id");
        }

        //String path = mapItem.getRootPath() + mapItem.getImageUrl().getOriginalUrl();
        String path = mapItem.getImageUrl().getOriginalUrl();

        File file = new File(path);
        if (!file.exists()){
            log.warn("download a resource that does not exist");
            return ResultUtils.error("download a resource that does not exist");
        }

        return ResultUtils.success();
    }

    @Override
    public void downloadSingleItem(String mapItemId, HttpServletResponse response) {

        MapItem mapItem = mapItemDao.findById(mapItemId);
        if (mapItem == null){
            log.warn("can not find item by this id");
            return;
        }

        //String path = mapItem.getRootPath() + mapItem.getImageUrl().getOriginalUrl();
        String loadPath = genericService.getLoadPath(mapItem.getServer());
        String path = loadPath + mapItem.getImageUrl().getOriginalUrl();

        File file = new File(path);
        if (!file.exists()){
            log.warn("download a resource that does not exist");
            return;
        }

        FileUtils.downloadFile(path, response);
    }

    @Override
    public void downloadBatchItem(List<String> mapItemIdList, HttpServletResponse response) {

        // String zipPath = resourcePath + mapItemDir + "/downloadZip/download.zip";
        String zipPath = resourcePath + mapItemDir + "/downloadZip/download.zip";
        List<File> fileList = new ArrayList<>();
        for (String id : mapItemIdList) {
            MapItem mapItem = mapItemDao.findById(id);
            if (mapItem == null){
                log.warn("download a resource that does not exist");
                return;
            }
            // String path = mapItem.getRootPath() + mapItem.getImageUrl().getOriginalUrl();
            String loadPath = genericService.getLoadPath(mapItem.getServer());
            String path = loadPath + mapItem.getImageUrl().getOriginalUrl();
            File file = new File(path);
            if (!file.exists()){
                log.warn("download a resource that does not exist");
                return;
            }
            fileList.add(file);
        }
        FileUtils.toZip(fileList, zipPath);


        FileUtils.downloadFile(zipPath, response);
    }

    @Override
    public JsonResult deleteSingleItem(String mapItemId) {

        try {
            MapItem mapItem = mapItemDao.findById(mapItemId);
            if(mapItem == null) {
                return ResultUtils.error("can not find mapItem by this id");
            }

            String errorMsg = "error:"; //返回的错误信息

            //删除关联的文件及文件夹
            ImageUrl imageUrl = mapItem.getImageUrl();
            //if(!FileUtils.deleteFile(mapItem.getRootPath() + imageUrl.getOriginalUrl()))
            //    errorMsg += " original";
            // if(!FileUtils.deleteFile(mapItem.getRootPath() + imageUrl.getThumbnailUrl()))
            //     errorMsg += " thumbnail";
            // if(!FileUtils.deleteDirectory(mapItem.getRootPath() + imageUrl.getTilesDir()))
            //     errorMsg += " tiles";
            String loadPath = genericService.getLoadPath(mapItem.getServer());
            if (mapItem.getThumbnailStatus() == StatusEnum.Finished){
                // if(!FileUtils.deleteFile(resourcePath + mapItem.getResourceDir() + imageUrl.getThumbnailUrl()))
                if(!FileUtils.deleteFile(loadPath + imageUrl.getThumbnailUrl()))
                    errorMsg += " thumbnail";
            }
            if (mapItem.getTileStatus() == StatusEnum.Finished){
                // if(!FileUtils.deleteDirectory(resourcePath + mapItem.getResourceDir() + imageUrl.getTilesDir()))
                if(!FileUtils.deleteDirectory(loadPath + imageUrl.getTilesDir()))
                    errorMsg += " tiles";
            }


            if (!errorMsg.equals("error:")){
                return ResultUtils.error(errorMsg);
            }


            mapItemDao.delete(mapItem);


        }catch (Exception e){
            return ResultUtils.error("database operation error");
        }
        return ResultUtils.success();
    }

    @Override
    public JsonResult deleteBatchItem(List<String> mapItemIdList) {
        List<String> errorIdList = new ArrayList<>();
        for (String id : mapItemIdList) {
            JsonResult jsonResult = deleteSingleItem(id);
            if (jsonResult.getCode() == -1){
                errorIdList.add(id);
            }
        }

        return errorIdList.size() == 0 ? ResultUtils.success() :
            ResultUtils.error("delete error id list: " + String.join(",", errorIdList));
    }

    @Override
    public JsonResult updateMetadata(String id, MapItemUpdateDTO mapItemUpdateDTO) {

        MapItem mapItem = mapItemDao.findById(id);
        if (mapItem == null)
            return ResultUtils.error("no item");

        //更新元数据
        // mapItem.setMetadata(mapItemUpdateDTO.getMetadata());
        mapItem.setMetadata(mapItemUpdateDTO.getMetadata());
        mapItem.setHasMatchMetaData(true);

        // 更新完元数据之后要重新计算地理坐标
        mapItem = genericService.setItemGeo(mapItem,mapItem.getMapCLSId());

        if (genericService.hasProcessFinish(mapItem)){
            mapItem.setHasNeedManual(false);
        }

        MapItem save = mapItemDao.save(mapItem);

        return ResultUtils.success(save);
    }


}
