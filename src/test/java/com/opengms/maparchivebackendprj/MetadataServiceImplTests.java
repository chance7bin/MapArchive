package com.opengms.maparchivebackendprj;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.dao.IMetadataDao;
import com.opengms.maparchivebackendprj.service.impl.MetadataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        String filename = "06-47-07.tif";
        String clsNameCn = "BASIC_SCALE_MAP_TWENTY";
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

}