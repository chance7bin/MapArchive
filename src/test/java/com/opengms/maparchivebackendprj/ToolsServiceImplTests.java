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
        checkDTO.setCheckDir("E:\\test\\image2\\1：10万");
        checkDTO.setMapCLSId("87086982-5ab1-473e-a65c-c010958f3ef3");
        JsonResult jsonResult = toolsService.statisticsMatchCount(checkDTO);
        System.out.println(jsonResult);
    }
}

