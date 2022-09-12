package com.opengms.maparchivebackendprj;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IClassificationTreeDao;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServer;
import com.opengms.maparchivebackendprj.entity.bo.config.DataServerList;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.ClassificationTree;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import com.opengms.maparchivebackendprj.service.IGeoInfoService;
import com.opengms.maparchivebackendprj.service.IMapItemService;
import com.opengms.maparchivebackendprj.service.impl.GeoInfoServiceImpl;
import com.opengms.maparchivebackendprj.utils.FileUtils;
import com.opengms.maparchivebackendprj.utils.XmlUtils;
import org.apache.poi.ss.formula.functions.T;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
class MapArchiveBackendPrjApplicationTests {

    @Autowired
    IMapItemCLSDao mapItemCLSDao;
    private Object DataServerList;

    @Test
    void contextLoads() {
    }

    @Test
    void generateBaseCLS(){

        for(MapClassification mapCLS : MapClassification.values()){
            System.out.println();

            MapItemCLS mapItemCLS = new MapItemCLS();
            mapItemCLS.setNameCn(mapCLS.getNameCn());
            mapItemCLS.setNameEn(mapCLS.getNameEn());

            mapItemCLSDao.insert(mapItemCLS);

        }

    }

    //当前目录与父级目录的子节点合并
    @Test
    void generateMiddleCLSMerge(){

        String currentName = "专题制图"; //当前目录中文名
        List<String> childrenName = new ArrayList<>(); //子集目录中文名列表
        String parentName = "地图制图";

        //创建当前目录
        MapItemCLS current = new MapItemCLS();
        current.setNameCn(currentName);

        //关联父级目录
        MapItemCLS parent = mapItemCLSDao.findByNameCn(parentName);
        if (parent == null){
            //不存在的话新创建一个并关联父子级目录
            parent = new MapItemCLS();
            parent.setNameCn(parentName);
            parent.getChildrenId().add(current.getId());
            current.setParentId(parent.getId());
        } else {
            //与父级目录的子节点合并
            parent.getChildrenId().add(current.getId());
            current.setParentId(parent.getId());

        }

    }


    //当前目录与父级目录的子节点不合并，即单独创建一个目录在两个目录之间
    @Test
    void generateMiddleCLSNoMerge(){

        String currentName = "专题制图"; //当前目录中文名
        List<String> childrenName = new ArrayList<>(); //子集目录中文名列表
        String parentName = "地图制图";

        //创建当前目录
        MapItemCLS current = new MapItemCLS();
        current.setNameCn(currentName);

        //关联父级目录
        MapItemCLS parent = mapItemCLSDao.findByNameCn(parentName);
        if (parent == null){
            //不存在的话新创建一个并关联父子级目录
            parent = new MapItemCLS();
            parent.setNameCn(parentName);
            parent.getChildrenId().add(current.getId());
            current.setParentId(parent.getId());
        } else {
            //与父级目录的子节点不合并
            if (parent.getChildrenId().size() == 0){
                parent.getChildrenId().add(current.getId());
            } else {
                List<String> childrenId = parent.getChildrenId();
                for (String child : childrenId) {
                    MapItemCLS c = mapItemCLSDao.findById(child);
                    c.setParentId(current.getId());
                    mapItemCLSDao.save(c);
                }
                current.setChildrenId(childrenId);
                parent.setChildrenId(Arrays.asList(current.getId()));
            }

            current.setParentId(parent.getId());

        }

    }


    @Test
    void getDirectoryTree(){

        List<MapItemCLS> clsList = mapItemCLSDao.findAll();

        //先生成一级目录，再往下一级生成
        List<MapItemCLS> firstLevel = new ArrayList<>();
        for (MapItemCLS mapItemCLS : clsList) {
            if (mapItemCLS.getParentId() == null){
                firstLevel.add(mapItemCLS);
            }
        }
        JSONArray tree = new JSONArray();
        for (MapItemCLS mapItemCLS : firstLevel) {
            tree.add(createDirectory(mapItemCLS.getId()));
        }

    }

    JSONObject createDirectory(String clsId){

        MapItemCLS cls = mapItemCLSDao.findById(clsId);

        JSONObject current = new JSONObject();
        current.put("id",cls.getId());
        current.put("nameCn",cls.getNameCn());

        List<String> childrenId = cls.getChildrenId();
        if (childrenId.size() != 0){
            JSONArray children = new JSONArray();
            for (String child : childrenId) {
                children.add(createDirectory(child));
            }
            current.put("children",children);
        }

        return current;

    }

    @Value("${resourcePath}")
    private String resourcePath;

    @Test
    void loadXml(){
        SAXReader reader = new SAXReader();
        try {
            Document read = reader.read(resourcePath + "/config/dataServer.xml");
            // 获取根节点
            Element root = read.getRootElement();
            // 通过elementIterator方法获取迭代器
            Iterator books = root.elementIterator();
            // 遍历迭代器
            while(books.hasNext()) {
                System.out.println("------------开始遍历------------");
                Element b = (Element)books.next();
                // 获取每本书的属性
                List<Attribute> bookList = b.attributes();
                for(Attribute a:bookList) {
                    System.out.println(a.getName() + ":" + a.getValue());
                }
                // 获取每本书下面的子节点
                Iterator childBook = b.elementIterator();
                while(childBook.hasNext()) {
                    Element c = (Element)childBook.next();
                    System.out.println(c.getName() + ":" + c.getStringValue());
                }
                System.out.println("-------------遍历完成------------");
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    void loadXml1() throws Exception {
        // 读取XML文件
        BufferedReader br = new BufferedReader(new FileReader(resourcePath + "/config/dataServer.xml"));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) !=null) {
            buffer.append(line);
        }
        br.close();
        // XML转为Java对象
        DataServerList dataServerList = (DataServerList) XmlUtils.xmlStrToObject(DataServerList.class, buffer.toString());
        // List<DataServer> dataServerList = (List<DataServer>) XmlUtils.xmlStrToObject(DataServer.class, buffer.toString());
        // return dataServerList.get();
        System.out.println();

    }

    T xml2Object(T t) throws Exception{
        // 读取XML文件
        BufferedReader br = new BufferedReader(new FileReader(resourcePath + "/config/dataServer.xml"));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) !=null) {
            buffer.append(line);
        }
        br.close();
        // XML转为Java对象
        T dataServerList = (T) XmlUtils.xmlStrToObject(T.class, buffer.toString());
        // List<DataServer> dataServerList = (List<DataServer>) XmlUtils.xmlStrToObject(DataServer.class, buffer.toString());
        // return dataServerList.get();
        System.out.println();
        return dataServerList;
    }

    @Resource(name="defaultDataServer")
    DataServer defaultDataServer;

    @Test
    void testProp(){
        System.out.println(defaultDataServer);
        System.out.println();
    }

    @Test
    void readProperties() throws IOException {
        Yaml yml = new Yaml();
        FileReader reader = new FileReader("src/main/resources/application.yml");
        BufferedReader buffer = new BufferedReader(reader);
        Map<String,Object> map = yml.load(buffer);
        // System.out.println(map.get("key1"));
        // System.out.println(map.get("key2"));
        buffer.close();
        reader.close();
    }

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Test
    void loadJson(){

        String path = resourcePath + "/config/collectionList.json";
        String s = FileUtils.readJsonFile(path);
        JSONArray collectionArr = JSON.parseArray(s);
        System.out.println();
        for (int i = 0; i < collectionArr.size(); i++) {
            JSONObject collection = collectionArr.getJSONObject(i);
            MetadataTable c = new MetadataTable();
            c.setName(collection.getString("name"));
            c.setCollection(collection.getString("collection"));
            if (metadataTableDao.findByCollection(c.getCollection()) == null){
                metadataTableDao.insert(c);
            }
        }

    }

    @Test
    void writeJson() throws IOException {
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
    }

    @Autowired
    IClassificationTreeDao classificationTreeDao;

    @Test
    void loadTree(){
        String path = resourcePath + "/config/classificationTree.json";
        String s = FileUtils.readJsonFile(path);
        JSONArray jsonArray = JSON.parseArray(s);
        System.out.println();
        ClassificationTree classificationTree = new ClassificationTree();
        classificationTree.setVersion("basic");
        classificationTree.setTree(jsonArray);
        classificationTreeDao.insert(classificationTree);
    }

    @Autowired
    IMapItemService mapItemService;

    @Test
    void buildClassifications(){
        List<String> list = mapItemService.buildClassifications("6e61e393-5765-4788-b43d-99d0252591d4");
        System.out.println();
    }


    @Autowired
    IMetadataDao metadataDao;

    @Test
    void metadataTest(){
        String collection = "BASIC_SCALE_MAP_TEN";
        // String formatFilename = "14-52-134(1966)";
        String formatFilename = "14-52-133";
        // List<JSONObject> list = metadataDao.findMetadataByOriginalNumAndYear(collection, formatFilename);
        List<JSONObject> list = metadataDao.findMetadataByOriginalNum(formatFilename, collection);

        System.out.println();

    }

    @Test
    void testSplit(){
        String loadPath = "D:/data/test2";
        String server = "D:/data";
        String[] split = loadPath.split(server);
        System.out.println(split[1]);

        System.out.println();

    }

    @Autowired
    GeoInfoServiceImpl GeoInfoService;
    @Test
    void getCoordinate(){
        String filename = "13-50-118-B-4.1965";
        String mapcls = "ea07a0e4-642d-46ee-b375-d58fa881f552";
        GeoInfo cordinate = GeoInfoService.getCoordinate(filename,mapcls);
        System.out.println(cordinate);
    }


}
