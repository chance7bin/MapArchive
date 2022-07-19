package com.opengms.maparchivebackendprj;

import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.CheckDTO;
import com.opengms.maparchivebackendprj.service.impl.ToolsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ToolsServiceImplTests {
    @Autowired
    ToolsServiceImpl toolsService;

    @Test
    void statisticsMatchCount() {
        CheckDTO checkDTO = new CheckDTO();
        checkDTO.setCheckDir("F:\\test\\Images\\1：100万");
        checkDTO.setMapCLSId("60d36541-e067-425a-a158-159cf0242306");
        JsonResult jsonResult = toolsService.statisticsMatchCount(checkDTO);
        System.out.println(jsonResult);
    }
}

