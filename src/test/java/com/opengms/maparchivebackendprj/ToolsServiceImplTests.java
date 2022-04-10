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
        checkDTO.setCheckDir("F:\\test\\Images\\1：2.5万");
        checkDTO.setMapCLSId("ea07a0e4-642d-46ee-b375-d58fa881f552");
        JsonResult jsonResult = toolsService.statisticsMatchCount(checkDTO);
        System.out.println(jsonResult);
    }
}

