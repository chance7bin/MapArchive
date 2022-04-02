package com.opengms.maparchivebackendprj;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMapItemCLSDao;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.MapItemCLS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class MapArchiveBackendPrjApplicationTests {

    @Autowired
    IMapItemCLSDao mapItemCLSDao;

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



}
