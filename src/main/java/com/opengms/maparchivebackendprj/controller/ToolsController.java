package com.opengms.maparchivebackendprj.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IClassificationTreeDao;
import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.dto.CheckDTO;
import com.opengms.maparchivebackendprj.entity.dto.ClassificationTreeDTO;
import com.opengms.maparchivebackendprj.entity.po.ClassificationTree;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.impl.ToolsServiceImpl;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Api(tags = "工具接口")
@RestController
@RequestMapping(value = "/tools")
@Slf4j
public class ToolsController {

    @Value("${resourcePath}")
    private String resourcePath;

    @Autowired
    IMapItemCLSDao mapItemCLSDao;

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Autowired
    IClassificationTreeDao classificationTreeDao;

    @Resource(name = "dataServerList")
    List<DataServer> dataServerList;
    @Autowired
    ToolsServiceImpl toolsService;

    @GetMapping("/test")
    public String test(){
        log.trace("trace");
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        return "hello world";
    }

    // @ApiOperation(value = "第一步，生成与数据库对应的表的目录" )
    // @RequestMapping(value = "/baseCLSMapDB", method = RequestMethod.GET)
    // JsonResult generateBaseCLS(){
    //
    //     List<MapClassification> mapClassifications = new ArrayList<>();
    //
    //     for(MapClassification mapCLS : MapClassification.values()){
    //         System.out.println();
    //
    //         MapItemCLS mapItemCLS = new MapItemCLS();
    //         mapItemCLS.setNameCn(mapCLS.getNameCn());
    //         mapItemCLS.setNameEn(mapCLS.getNameEn());
    //
    //         mapItemCLSDao.insert(mapItemCLS);
    //
    //         mapClassifications.add(mapCLS);
    //
    //     }
    //
    //     return ResultUtils.success(mapClassifications);
    // }
    //
    // @ApiOperation(value = "第二步，生成数据库表上一级目录 childrenName必须是MapClassification中的" )
    // @RequestMapping(value = "/baseCLSPreLevel", method = RequestMethod.POST)
    // JsonResult generateBaseCLSPreLevel(@RequestBody MapItemMapDBCLSDTO dto){
    //
    //     String currentName = dto.getCurrentName();
    //     List<MapClassification> childrenName = dto.getChildrenName();
    //     //创建当前目录
    //     MapItemCLS current = new MapItemCLS();
    //     current.setNameCn(currentName);
    //
    //     for (MapClassification cls : childrenName) {
    //         MapItemCLS byNameEn = mapItemCLSDao.findByNameEn(cls.getNameEn());
    //         byNameEn.setParentId(current.getId());
    //         mapItemCLSDao.save(byNameEn);
    //         current.getChildrenId().add(byNameEn.getId());
    //     }
    //
    //     mapItemCLSDao.insert(current);
    //
    //     return ResultUtils.success(current);
    //
    // }
    //
    // @ApiOperation(value = "第三步，生成其他级别的目录 childrenId传入目录的id" )
    // @RequestMapping(value = "/CLSOtherLevel", method = RequestMethod.POST)
    // JsonResult generateCLSOtherLevel(@RequestBody MapItemCLSDTO dto){
    //
    //     String currentName = dto.getCurrentName();
    //     List<String> childrenId = dto.getChildrenId();
    //     //创建当前目录
    //     MapItemCLS current = new MapItemCLS();
    //     current.setNameCn(currentName);
    //
    //     for (String cls : childrenId) {
    //         MapItemCLS byNameEn = mapItemCLSDao.findById(cls);
    //         if (byNameEn == null){
    //             return ResultUtils.error("错误的id:" + cls);
    //         }
    //     }
    //
    //
    //     for (String cls : childrenId) {
    //         MapItemCLS byNameEn = mapItemCLSDao.findById(cls);
    //         byNameEn.setParentId(current.getId());
    //         mapItemCLSDao.save(byNameEn);
    //         current.getChildrenId().add(byNameEn.getId());
    //     }
    //     mapItemCLSDao.insert(current);
    //
    //     return ResultUtils.success(current);
    //
    // }
    //
    //
    //
    //
    // @ApiOperation(value = "新增与数据库对应的表的目录 mapCLSList:MapClassification的nameEn" )
    // @RequestMapping(value = "/baseCLSMapDB/addition", method = RequestMethod.POST)
    // // JsonResult generateBaseCLSAddition(@RequestBody List<String> mapCLSNameEnList){
    // JsonResult generateBaseCLSAddition(@RequestBody List<MapClassification> mapClassifications){
    //
    //     // List<MapClassification> mapClassifications = new ArrayList<>();
    //     //
    //     // for (String clsNameEn : mapCLSNameEnList) {
    //     //     MapClassification cls = MapClassification.getMapCLSByNameEn(clsNameEn);
    //     //     if (cls == null){
    //     //         return ResultUtils.error("错误的clsNameEn:" + clsNameEn);
    //     //     }
    //     //     mapClassifications.add(cls);
    //     // }
    //
    //
    //     List<MapItemCLS> newList = new ArrayList<>();
    //     for (MapClassification mapCLS : mapClassifications) {
    //         MapItemCLS mapItemCLS = new MapItemCLS();
    //         mapItemCLS.setNameCn(mapCLS.getNameCn());
    //         mapItemCLS.setNameEn(mapCLS.getNameEn());
    //
    //         mapItemCLSDao.insert(mapItemCLS);
    //
    //         newList.add(mapItemCLS);
    //     }
    //
    //
    //
    //     return ResultUtils.success(newList);
    // }
    //
    // @ApiOperation(value = "把A目录关联到B目录的子目录下" )
    // @RequestMapping(value = "/A2B/{parentId}/{childId}", method = RequestMethod.GET)
    // JsonResult a2b(@PathVariable String parentId, @PathVariable String childId){
    //
    //     MapItemCLS parent = mapItemCLSDao.findById(parentId);
    //     MapItemCLS child = mapItemCLSDao.findById(childId);
    //
    //     if (parent == null || child == null){
    //         return ResultUtils.error("传入的parentId或childId有错误");
    //     }
    //
    //     parent.getChildrenId().add(childId);
    //     child.setParentId(parentId);
    //     mapItemCLSDao.save(parent);
    //     mapItemCLSDao.save(child);
    //
    //     return ResultUtils.success();
    //
    //
    // }

    //
    // @ApiOperation(value = "当前目录与父级目录的子节点合并" )
    // @RequestMapping(value = "/generateMiddleCLSMerge", method = RequestMethod.POST)
    // JsonResult generateMiddleCLSMerge(@RequestBody MapItemCLSDTO mapItemCLSDTO){
    //
    //     String currentName = "专题制图"; //当前目录中文名
    //     List<String> childrenName = new ArrayList<>(); //子集目录中文名列表
    //     String parentName = "地图制图";
    //
    //     // String currentName = mapItemCLSDTO.getCurrentName();
    //     // List<String> childrenName = mapItemCLSDTO.getChildrenName();
    //     // String parentName = mapItemCLSDTO.getParentName();
    //
    //
    //     //创建当前目录
    //     MapItemCLS current = new MapItemCLS();
    //     current.setNameCn(currentName);
    //
    //     //关联父级目录
    //     MapItemCLS parent = mapItemCLSDao.findByNameCn(parentName);
    //     if (parent == null){
    //         //不存在的话新创建一个并关联父子级目录
    //         parent = new MapItemCLS();
    //         parent.setNameCn(parentName);
    //         parent.getChildrenId().add(current.getId());
    //         current.setParentId(parent.getId());
    //     } else {
    //         //与父级目录的子节点合并
    //         parent.getChildrenId().add(current.getId());
    //         current.setParentId(parent.getId());
    //
    //     }
    //
    //     //TODO 关联子集目录
    //
    //     return ResultUtils.success();
    //
    // }
    //
    //
    // //当前目录与父级目录的子节点不合并，即单独创建一个目录在两个目录之间
    // @ApiOperation(value = "当前目录与父级目录的子节点不合并，即单独创建一个目录在两个目录之间" )
    // @RequestMapping(value = "/generateMiddleCLSNoMerge", method = RequestMethod.POST)
    // JsonResult generateMiddleCLSNoMerge(){
    //
    //     String currentName = "专题制图"; //当前目录中文名
    //     List<String> childrenName = new ArrayList<>(); //子集目录中文名列表
    //     String parentName = "地图制图";
    //
    //     //创建当前目录
    //     MapItemCLS current = new MapItemCLS();
    //     current.setNameCn(currentName);
    //
    //     //关联父级目录
    //     MapItemCLS parent = mapItemCLSDao.findByNameCn(parentName);
    //     if (parent == null){
    //         //不存在的话新创建一个并关联父子级目录
    //         parent = new MapItemCLS();
    //         parent.setNameCn(parentName);
    //         parent.getChildrenId().add(current.getId());
    //         current.setParentId(parent.getId());
    //     } else {
    //         //与父级目录的子节点不合并
    //         if (parent.getChildrenId().size() == 0){
    //             parent.getChildrenId().add(current.getId());
    //         } else {
    //             List<String> childrenId = parent.getChildrenId();
    //             for (String child : childrenId) {
    //                 MapItemCLS c = mapItemCLSDao.findById(child);
    //                 c.setParentId(current.getId());
    //                 mapItemCLSDao.save(c);
    //             }
    //             current.setChildrenId(childrenId);
    //             parent.setChildrenId(Arrays.asList(current.getId()));
    //         }
    //
    //         current.setParentId(parent.getId());
    //
    //     }
    //
    //     //TODO 关联子集目录
    //
    //
    //     return ResultUtils.success();
    // }


    // @ApiOperation(value = "得到目录树" )
    // @RequestMapping(value = "/getDirectoryTree", method = RequestMethod.GET)
    // JsonResult getDirectoryTree(){
    //
    //     List<MapItemCLS> clsList = mapItemCLSDao.findAll();
    //
    //     //先生成一级目录，再往下一级生成
    //     List<MapItemCLS> firstLevel = new ArrayList<>();
    //     for (MapItemCLS mapItemCLS : clsList) {
    //         if (mapItemCLS.getParentId() == null){
    //             firstLevel.add(mapItemCLS);
    //         }
    //     }
    //     JSONArray tree = new JSONArray();
    //     for (MapItemCLS mapItemCLS : firstLevel) {
    //         tree.add(createDirectory(mapItemCLS.getId()));
    //     }
    //
    //     return ResultUtils.success(tree);
    //
    // }
    //
    // JSONObject createDirectory(String clsId){
    //
    //     MapItemCLS cls = mapItemCLSDao.findById(clsId);
    //
    //     JSONObject current = new JSONObject();
    //     current.put("id",cls.getId());
    //     current.put("nameCn",cls.getNameCn());
    //
    //     List<String> childrenId = cls.getChildrenId();
    //     if (childrenId.size() != 0){
    //         JSONArray children = new JSONArray();
    //         for (String child : childrenId) {
    //             children.add(createDirectory(child));
    //         }
    //         current.put("children",children);
    //     }
    //
    //     return current;
    //
    // }

    @ApiOperation(value = "得到目录树" )
    @RequestMapping(value = "/directoryTree/{version}", method = RequestMethod.GET)
    JsonResult getDirectoryTreeByVersion(@PathVariable String version){
        ClassificationTree tree = classificationTreeDao.findByVersion(version);
        return tree == null ? ResultUtils.error() : ResultUtils.success(tree.getTree());
    }


    @ApiOperation(value = "元数据表格列表入库 collectionList放在/config/collectionList.json中" )
    @RequestMapping(value = "/loadCollectionList", method = RequestMethod.GET)
    public JsonResult loadCollectionList(){
        String path = resourcePath + "/config/collectionList.json";
        String s = FileUtils.readJsonFile(path);
        JSONArray collectionArr = JSON.parseArray(s);
        // System.out.println();
        for (int i = 0; i < collectionArr.size(); i++) {
            JSONObject collection = collectionArr.getJSONObject(i);
            MetadataTable c = new MetadataTable();
            c.setName(collection.getString("name"));
            c.setCollection(collection.getString("collection"));
            if (metadataTableDao.findByCollection(c.getCollection()) == null){
                metadataTableDao.insert(c);
            }
        }

        return ResultUtils.success();
    }

    @ApiOperation(value = "loadCollectionList后，自动匹配id并重新写入到json文件中" )
    @RequestMapping(value = "/updateCollectionList", method = RequestMethod.GET)
    public JsonResult updateCollectionList() throws IOException {
        String path = resourcePath + "/config/collectionList.json";
        String s = FileUtils.readJsonFile(path);
        JSONArray collectionArr = JSON.parseArray(s);
        for (int i = 0; i < collectionArr.size(); i++) {
            JSONObject collection = collectionArr.getJSONObject(i);
            if (collection.getString("id").equals("")){
                MetadataTable c = metadataTableDao.findByCollection(collection.getString("collection"));
                collection.put("id",c.getId());
            }
        }

        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
        osw.write(collectionArr.toString());
        osw.flush();//清空缓冲区，强制输出数据
        osw.close();//关闭输出流

        return ResultUtils.success();
    }

    @ApiOperation(value = "分类树入库" )
    @RequestMapping(value = "/loadClassificationTree", method = RequestMethod.POST)
    public JsonResult loadClassificationTree(@RequestBody ClassificationTreeDTO classificationTreeDTO){
        // String path = resourcePath + "/config/classificationTree.json";
        String s = FileUtils.readJsonFile(classificationTreeDTO.getPath());
        JSONArray jsonArray = JSON.parseArray(s);
        // System.out.println();

        ClassificationTree tree = classificationTreeDao.findByVersion(classificationTreeDTO.getVersion());

        if (tree == null){
            ClassificationTree classificationTree = new ClassificationTree();
            classificationTree.setVersion(classificationTreeDTO.getVersion());
            classificationTree.setTree(jsonArray);
            ClassificationTree insert = classificationTreeDao.insert(classificationTree);

            return ResultUtils.success(insert);
        } else {
            tree.setTree(jsonArray);
            ClassificationTree save = classificationTreeDao.save(tree);
            return ResultUtils.success(save);
        }


    }

    @ApiOperation(value = "文件名匹配详情" )
    @RequestMapping(value = "/statisticsMatchCount", method = RequestMethod.GET)
    public JsonResult statisticsMatchCount(@RequestBody CheckDTO checkDTO){
        return toolsService.statisticsMatchCount(checkDTO);
    }

    @ApiOperation(value = "得到uuid" )
    @RequestMapping(value = "/getUUID", method = RequestMethod.GET)
    public JsonResult getUUID(){
        GenericId genericId = new GenericId();
        return ResultUtils.success(genericId.getId());
    }


    @ApiOperation(value = "得到 /config/dataServer.xml的服务器信息" )
    @RequestMapping(value = "/dataServer", method = RequestMethod.GET)
    public JsonResult getDataServer(){
        // GenericId genericId = new GenericId();
        return ResultUtils.success(dataServerList);
    }


}
