package com.opengms.maparchivebackendprj;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.dao.impl.MapItemDaoImpl;
import com.opengms.maparchivebackendprj.service.impl.MapItemServiceImpl;
import com.opengms.maparchivebackendprj.service.impl.MetadataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class MetadataServiceImplTests {
    @Autowired
    MetadataServiceImpl metadataService;
    @Autowired
    IMetadataDao metadataDao;
    @Test
    void getBSMMetadata() {
        String filename = "05-48-008-A-1(1958).tif";
        String clsNameCn = "BASIC_SCALE_MAP_TWO_DOT_FIVE";
        String excelPath = "";
        Map map = null;
        try {
            map = metadataService.getBSMMetadata(filename, clsNameCn, excelPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(map);
    }

    @Test
    void findMetadataByOriginalNumAndYear(){
        String filename = "02-47-070(1965)";
        String collection = "BASIC_SCALE_MAP_TEN";
        List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(filename,collection);
        System.out.println(list1);
    }

    @Test
    void findMetadataByOriginalNum(){
        String filename = "14-52-133";
        String collection = "BASIC_SCALE_MAP_TEN";
        List<JSONObject> list1 = metadataDao.findMetadataByOriginalNum(filename,collection);
        System.out.println(list1);
    }
    @Autowired
    MapItemDaoImpl mapItemDaoImpl;

    @Test
    void findBySearchTextAndPageable(){
        String curQueryField = "name";
        String searchText = "12-45-064-A";
//        List<String> mapClassifications = mapItemServiceImpl.buildClassifications("1262f876-d5f4-4406-8bc7-177f9f89cd38");
        List<String> clsIdList = new ArrayList<>();
        clsIdList.add("1262f876-d5f4-4406-8bc7-177f9f89cd38");
//        mapItemDaoImpl.findBySearchTextAndPageable(curQueryField, searchText, clsIdList);

    }


}