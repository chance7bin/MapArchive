package com.opengms.maparchivebackendprj;

import com.opengms.maparchivebackendprj.service.impl.MetadataServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class MetadataServiceImplTests {
    @Autowired
    MetadataServiceImpl metadataService;
    @Test
    void getBSMMetadata() {
        String filename = "13-53-(33,34)(1969).tif";
        String clsNameCn = "BASIC_SCALE_MAP_TWENTY";
        String excelPath = "F:\\test\\20w_whole.xlsx";
        Map map = null;
        try {
            map = metadataService.getBSMMetadata(filename, clsNameCn, excelPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(map);
    }
}
