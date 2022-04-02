package com.opengms.maparchivebackendprj.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/04
 */
@Data
public class ExcelPathDTO {
    @ApiModelProperty(value = "excel路径", example = "E:\\mapArchiveFiles\\DEMO.xls")
    String excelPath;
}
