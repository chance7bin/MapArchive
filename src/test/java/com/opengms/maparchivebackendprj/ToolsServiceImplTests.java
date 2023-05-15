package com.opengms.maparchivebackendprj;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.CheckDTO;
import com.opengms.maparchivebackendprj.service.IMatchDataService;
import com.opengms.maparchivebackendprj.service.impl.MetadataMatchServiceImpl;
import com.opengms.maparchivebackendprj.service.impl.MetadataServiceImpl;
import com.opengms.maparchivebackendprj.service.impl.ToolsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class ToolsServiceImplTests {
    @Autowired
    ToolsServiceImpl toolsService;

    @Autowired
    MetadataMatchServiceImpl metadataMatchService;

    @Autowired
    MetadataServiceImpl metadataServiceImpl;

    @Test
    void statisticsMatchCount() {
        CheckDTO checkDTO = new CheckDTO();
        checkDTO.setCheckDir("E:\\test\\image2\\1：10万");
        checkDTO.setMapCLSId("87086982-5ab1-473e-a65c-c010958f3ef3");
        JsonResult jsonResult = toolsService.statisticsMatchCount(checkDTO);
        System.out.println(jsonResult);
    }
    @Test
    void test(){
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        System.out.println(pattern.matcher("中").matches());
    }
    @Test
    void test1(){
        List list = new ArrayList<String>();
        List map_list = new ArrayList<String>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "F:\\classDATA\\处理程序\\jg_test.txt"));
            String line = reader.readLine();
            while (line != null) {
                // read next line
                list.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List list_error = new ArrayList<String>();
        list.forEach((e) -> {
            e = e + "";
            String filename = (String) e;
            Map<String, Object> map = null;
            try {
//                Map<String,String> mapInfo = new HashMap<>();
//                mapInfo.put("type","ONE");
//                mapInfo.put("matchField","图幅编号");
//                mapInfo.put("matchFieldAndYear","numAndYear");
//                map = metadataServiceImpl.getBSMMetadata(filename,mapInfo, collection, "", excelPath);
//                map = metadataMatchService.getAmericaMetadata(filename,"M07_01_07_02_01", "", null);
                map = metadataServiceImpl.getMetadataByFilenameByType(filename, "fa8874aa-f488-4e7e-abbc-7bebb7657dc0", "", null);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (map == null){
                list_error.add(e);
            }
        });
        System.out.print(list_error);
        System.out.print(list.size());
    }

    @Test
    void test2() throws Exception {
//        Map<String, Object> map = metadataMatchService.getAmericaMetadata("E-05-01-01.tif","M07_01_07_02_01","",null);
        Map<String, Object> map = metadataServiceImpl.getMetadataByFilenameByType("13-30-93.81.tif", "fa8874aa-f488-4e7e-abbc-7bebb7657dc0", "", null);
        System.out.print(map);
    }
    @Test
    void test3() throws Exception {
        // 判断是否包含中文
//        String address = "07-50-宜-1.tif";
//        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
//        Matcher m = p.matcher(address);
//        System.out.print(m.find());

        // 判断是否全为数字
        String char_sign = "7-";
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Boolean a = pattern.matcher(char_sign).matches();
        System.out.print(a);
    }
}

