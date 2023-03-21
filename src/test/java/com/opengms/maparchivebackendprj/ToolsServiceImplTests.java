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
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "G:\\classDATA\\文件名\\jg_test.txt"));
            String line = reader.readLine();
            while (line != null) {
                // read next line
                line = reader.readLine();
                list.add(line);
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
                map = metadataServiceImpl.getBSMMetadata(filename,"BASIC_SCALE_MAP_ONE","地形图", null);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (map == null){
                list_error.add(e);
            }
        });
        System.out.print(list_error);
    }

    @Test
    void test2() throws Exception {
        Map<String, Object> map = metadataServiceImpl.getBSMMetadata("06-49-060-C-3-(3).tif","BASIC_SCALE_MAP_ONE","地形图",null);
        System.out.print(map);
    }
}

