package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.dao.IMapItemDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ImageUrl;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import com.opengms.maparchivebackendprj.entity.dto.mapItem.*;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import com.opengms.maparchivebackendprj.service.*;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ProcessParam;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.enums.StatusEnum;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import com.opengms.maparchivebackendprj.entity.po.MapItem;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ImageUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJson;
import org.springframework.data.mongodb.core.geo.GeoJsonMultiPolygon;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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



    // 异步调用处理任务，防止请求阻塞
    @Async
    @Override
    public void process(ProcessDTO processDTO, String username, MapItemCLS mapItemCLS) {

        log.info("process invoke begin [ {} ]", new Date());

        String processingPath = processDTO.getProcessingPath();
//        String savePath = processDTO.getSavePath();

        //把路径中的 \ 都替换为 /
        processingPath = processingPath.replace("\\", "/");
//        savePath = savePath.replace("\\", "/");

        // 自动生成保存路径
        String cls = mapItemCLS.getNameEn();
        String savePath = resourcePath +  "/" + cls;

        MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls);

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

        List<String> itemListId = new ArrayList<>();
        for (File file1 : fileList) {

            //创建实体
            MapItem mapItem = new MapItem();

            mapItem.setName(file1.getName());
            mapItem.setAuthor(username);
            mapItem.setMapCLS(mapCLSByNameEn);
            mapItemDao.insert(mapItem);

            itemListId.add(mapItem.getId());

            initMapItem(mapItem,file1.getPath(),savePath,processingPath, processDTO ,null);

        }

        logDao.insert(new LogInfo(username,itemListId, OperateTypeEnum.UPLOAD,new Date()));


    }

    @Async
    @Override
    public void insert(MapItemAddDTO mapItemAddDTO, String username, MapItemCLS mapItemCLS) {



        String processingPath = resourcePath +  "/" + mapItemCLS.getNameEn() + "/file";
        String savePath = resourcePath +  "/" + mapItemCLS.getNameEn();


        String cls = mapItemCLS.getNameEn();

        MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls);

        // List<MapItem> mapItemList = new ArrayList<>();
        List<String> itemListId = new ArrayList<>();
        List<FileInfo> fileInfoList = mapItemAddDTO.getFileInfoList();
        for (FileInfo fileInfo : fileInfoList) {
            // MapItem mapItem = new MapItem();
            MapItem mapItem = new MapItem();
            mapItem.setAuthor(username);
            mapItem.setName(fileInfo.getFileName());
            mapItem.setMapCLS(mapCLSByNameEn);
            mapItemDao.insert(mapItem);

            itemListId.add(mapItem.getId());

            ProcessDTO processDTO = new ProcessDTO(
                processingPath,
                mapItemCLS.getId(),
                mapItemAddDTO.getMetadataTable(),
                mapItemAddDTO.isMatchMetadata(),
                mapItemAddDTO.isCalcGeoInfo(),
                mapItemAddDTO.isGenerateThumbnail(),
                mapItemAddDTO.isGenerateTiles());

            // mapItemList.add(initMapItem(mapItem,fileInfo.getPath(),savePath,processingPath, processDTO, fileInfo));
            initMapItem(mapItem,fileInfo.getPath(),savePath,processingPath, processDTO, fileInfo);
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


        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());


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

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

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

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());


        List<MapItem> mapItemList =
            mapItemDao.findBySearchTextAndPageable(curQueryField, searchText, mapClassifications, pageable);


        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult countMapItemList(SpecificFindDTO findDTO) {

        String searchText = findDTO.getSearchText();
        String curQueryField = findDTO.getCurQueryField();

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());


        long count =
            mapItemDao.countBySearchText(curQueryField, searchText, mapClassifications);


        return ResultUtils.success(count);


    }


    private PageableResult<MapItem> findBySearchTextAndPolygonAndPageable(
        String curQueryField, String searchText,
        GeoJsonPolygon polygon, String mapCLSId, Pageable pageable){

        List<MapClassification> mapClassifications = buildClassifications(mapCLSId);

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndPolygonAndPageable(curQueryField,searchText,polygon,mapClassifications,pageable);

        long count = mapItemDao.countBySearchTextAndPolygon(curQueryField, searchText, polygon, mapClassifications);

        return new PageableResult<>(count,mapItemList);

    }


    /**
     * 构建classifications集合,如果没有childrenId则表示只查询一个分类条目，有的话表示有多个分类条目
     * @param clsId
     * @return java.util.List<com.opengms.maparchivebackendprj.entity.enums.MapClassification>
     * @Author bin
     **/
    private List<MapClassification> buildClassifications(String clsId){
        List<MapClassification> classifications = new ArrayList<>();

        // 判断是否传入clsId
        if (genericService.isEmptyString(clsId))
            return classifications;

        MapItemCLS cls = mapItemCLSDao.findById(clsId);
        //判断该cls是否存在
        if (!genericService.isExist(cls))
            return classifications;

        List<String> childrenId = cls.getChildrenId();
        if (childrenId == null || childrenId.size() == 0){
            if (cls.getNameEn() != null){
                MapClassification mapCLSByNameEn = MapClassification.getMapCLSByNameEn(cls.getNameEn());
                if (mapCLSByNameEn != null){
                    classifications.add(mapCLSByNameEn);
                }
            }
        }
        else {
            for (String child : childrenId) {
                classifications.addAll(buildClassifications(child));
            }
        }
        return classifications;
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


    /**
     * 地图条目入库
     * @param mapItem 地图条目
     * @param loadPath 加载的文件路径
     * @param savePath 生成新文件保存的路径
     * @param rootPath 在多级目录的情况下，以该路径作为根路径进行切分，按照该路径后面的格式生成对应的文件夹
     * @param processDTO 批量处理所传入的参数
     * @param fileInfo 与地图条目关联的文件信息(上传的功能才关联)
     * @return com.example.maparchivebackend.entity.po.MapItem
     * @Author bin
     **/
    private MapItem initMapItem(
        MapItem mapItem,
        String loadPath, String savePath, String rootPath,
        ProcessDTO processDTO,FileInfo fileInfo){

        // MapItem mapItem = new MapItem();
        mapItem.setProcessStatus(StatusEnum.Started);

        //文件与地图条目进行关联
        if(fileInfo != null){
            mapItem.setRelativeFileId(fileInfo.getId());
        }


        if (processDTO.isMatchMetadata()){
            mapItem = matchMetadata(mapItem,mapItem.getMapCLS(),processDTO.getMetadataTable());

        }

        if (processDTO.isCalcGeoInfo()){
            mapItem = setItemGeo(mapItem, mapItem.getMapCLS());
        }


        String type = FileUtils.getFileType(mapItem.getName());
        // 执行脚本的文件后缀必须为 .jpg 或者是 .png  或者是.tif .tiff
        if (type.equals("jpg") || type.equals("png")
            || type.equals("tif") || type.equals("tiff")
            || type.equals("TIF") || type.equals("TIFF")){

            //加载的文件路径
            // String filePath = loadPath;
            //新文件保存路径
            String thumbnailPath = "/" + "thumbnail";
            String tilesPath = "/" + "tiles";
            //对多级目录的处理
            String[] path = FileUtils.buildMultiDirPath(loadPath, rootPath, thumbnailPath, tilesPath);

            File loadFile = new File(loadPath);

            // 得到图片的元数据
            try {
                // TODO: 2022/3/16 正常图片的宽度应该大于高度，如果不是这样的话可能图片需要做旋转的处理
                mapItem.setImageMetadata(ImageUtils.getImageInfo(loadPath));
            } catch (Exception e){
                mapItem.setImageMetadata(null);
            }

            path[1] += "/" + FileUtils.getFilenameNoSuffix(loadFile);

            // 异步处理地图
            if (processDTO.isGenerateThumbnail()){
                generateThumbnailImage(mapItem,loadPath,savePath + path[0]);
            }

            if (processDTO.isGenerateTiles()){
                generateTiles(mapItem,loadPath,savePath + path[1]);
            }

            // 如果既没有生成缩略图也没有切片，那处理的状态设置为Finished
            if (!processDTO.isGenerateThumbnail() && !processDTO.isGenerateTiles()){
                mapItem.setProcessStatus(StatusEnum.Finished);
            }

            if (asyncService.hasProcessFinish(mapItem)){
                mapItem.setHasNeedManual(false);
            }


            // 设置生成的图片路径
            // mapItem中存的都是相对路径，定位文件位置用rootPath定位
//            String originalUrl = fileInfo.getPath().replace("\\", "/");
//            savePath = savePath.replace("\\", "/");
            mapItem.getImageUrl().setOriginalUrl(loadPath);
            String filename = FileUtils.getFilenameNoSuffix(loadFile);
            filename = "thumb_" + filename + ".png";
            mapItem.getImageUrl().setThumbnailUrl(path[0] + "/" + filename);
            mapItem.getImageUrl().setTilesDir(path[1]);
            // mapItem.setRootPath(savePath);
            mapItem.setResourceDir(savePath.split(resourcePath)[1]);
            mapItem.setProcessParam(new ProcessParam(mapItem.getId(), loadPath,savePath + path[0],savePath + path[1]));
        }


        return mapItemDao.save(mapItem);
    }





    @Override
    public JsonResult getProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult getProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, false, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);


    }

    @Override
    public JsonResult getProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);

    }

    @Override
    public JsonResult getProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        Pageable pageable = genericService.getPageable(findDTO);

        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        List<MapItem> mapItemList = mapItemDao.findBySearchTextAndStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, mapClassifications,pageable);

        return ResultUtils.success(mapItemList);
    }

    @Override
    public JsonResult countProcessingListStatusIsProcessing(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListStatusIsFinished(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, false,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListNeedManual(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatusAndHasNeedManual(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums, true,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public JsonResult countProcessingListStatusIsError(SpecificFindDTO findDTO, List<StatusEnum> statusEnums) {
        List<MapClassification> mapClassifications = buildClassifications(findDTO.getMapCLSId());

        long count = mapItemDao.countByStatus(findDTO.getCurQueryField(),findDTO.getSearchText(),statusEnums,mapClassifications);

        return ResultUtils.success(count);
    }

    @Override
    public void batchProcess(BatchProcessDTO processDTO) {

        FindDTO findDTO = new FindDTO(1,processDTO.getProcessCount(),false,"createTime");

        Pageable pageable = genericService.getPageable(findDTO);

        List<MapItem> mapItemList = mapItemDao.findByStatus(Arrays.asList(StatusEnum.Finished),pageable);

        for (MapItem mapItem : mapItemList) {

            ProcessParam param = mapItem.getProcessParam();

            mapItem.setProcessStatus(StatusEnum.Started);

            if (processDTO.isMatchMetadata() && !mapItem.isHasMatchMetaData()){
                mapItem = matchMetadata(mapItem, mapItem.getMapCLS(), null);
            }

            if (processDTO.isCalcGeoInfo() && !mapItem.isHasCalcCoordinate()){
                mapItem = setItemGeo(mapItem,mapItem.getMapCLS());
            }

            if (processDTO.isGenerateThumbnail() && (mapItem.getThumbnailStatus() != StatusEnum.Finished)){
                generateThumbnailImage(mapItem,param.getInputPath(),param.getThumbnailOutputDir());
            }

            if (processDTO.isGenerateTiles() && (mapItem.getTileStatus() != StatusEnum.Finished)){
                generateTiles(mapItem,param.getInputPath(),param.getTilesOutputDir());
            }

            // 如果既没有生成缩略图也没有切片，那处理的状态设置为Finished
            if (!processDTO.isGenerateThumbnail() && !processDTO.isGenerateTiles()){
                mapItem.setProcessStatus(StatusEnum.Finished);
            }

            mapItemDao.save(mapItem);

        }

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
        String path = mapItem.getImageUrl().getOriginalUrl();

        File file = new File(path);
        if (!file.exists()){
            log.warn("download a resource that does not exist");
            return;
        }

        FileUtils.downloadFile(path, response);
    }

    @Override
    public void downloadBatchItem(List<String> mapItemIdList, HttpServletResponse response) {

        String zipPath = resourcePath + mapItemDir + "/downloadZip/download.zip";
        List<File> fileList = new ArrayList<>();
        for (String id : mapItemIdList) {
            MapItem mapItem = mapItemDao.findById(id);
            if (mapItem == null){
                log.warn("download a resource that does not exist");
                return;
            }
            // String path = mapItem.getRootPath() + mapItem.getImageUrl().getOriginalUrl();
            String path = mapItem.getImageUrl().getOriginalUrl();
            File file = new File(path);
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

            if(!FileUtils.deleteFile(resourcePath + mapItem.getResourceDir() + imageUrl.getThumbnailUrl()))
                errorMsg += " thumbnail";
            if(!FileUtils.deleteDirectory(resourcePath + mapItem.getResourceDir() + imageUrl.getTilesDir()))
                errorMsg += " tiles";

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
    public JsonResult updateItem(String id, MapItemUpdateDTO mapItemUpdateDTO) {

        MapItem mapItem = mapItemDao.findById(id);
        if (mapItem == null)
            return ResultUtils.error("no item");

        //更新元数据
        // mapItem.setMetadata(mapItemUpdateDTO.getMetadata());
        mapItem.setMetadata(mapItemUpdateDTO.getMetadata());

        // 更新完元数据之后要重新计算地理坐标
        mapItem = setItemGeo(mapItem,mapItem.getMapCLS());

        mapItemDao.save(mapItem);

        return ResultUtils.success();
    }


    /**
     * 匹配元数据
     * @param mapItem 地图类
     * @param mapCLS 地图分类
     * @param excelPath excel路径
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @Author bin
     **/
    private MapItem matchMetadata(MapItem mapItem, MapClassification mapCLS, String excelPath){

        Map<String, Object> metadataByName = null;
        try {
            metadataByName = metadataService.getMetadataByFilenameByType(mapItem.getName(), mapCLS, excelPath);
        }catch (Exception e){
            mapItem.setHasMatchMetaData(false);
            return mapItem;
        }
        if (metadataByName == null){
            mapItem.setHasMatchMetaData(false);
            // mapItem.setHasNeedManual(true);
        }else {
            mapItem.setMetadata(metadataByName);
            mapItem.setHasMatchMetaData(true);
        }

        return mapItem;

    }


    // 设置条目的地理坐标(center,box)
    private MapItem setItemGeo(MapItem mapItem, MapClassification mapCLS){

        // 废弃
        // String fileName = mapItem.getName();

        GeoInfo coordinate;

        try {
            String fileName = (String) mapItem.getMetadata().get("档号");
            coordinate = geoInfoService.getCoordinate(fileName, mapCLS);
        }catch (Exception e){
            coordinate = null;
        }

        if (coordinate != null){
            mapItem.setCenter(coordinate.getCenter());
            mapItem.setPolygon(coordinate.getPolygon());
            mapItem.setHasCalcCoordinate(true);
            // mapItem.setHasNeedManual(false);
        } else {
            mapItem.setHasCalcCoordinate(false);
            // mapItem.setHasNeedManual(true);
        }

        return mapItem;
    }


    //生成缩略图
    private void generateThumbnailImage(MapItem mapItem, String inputPath, String outputDir){

        asyncService.generateThumbnailImage(mapItem.getId(),inputPath,outputDir);


    }


    //切片处理
    private void generateTiles(MapItem mapItem, String inputPath, String outputDir){

        asyncService.generateTiles(mapItem.getId(),inputPath,outputDir);

    }

}
