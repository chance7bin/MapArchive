package com.opengms.maparchivebackendprj.entity.dto;

import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
@Data
public class SpecificFindDTO extends FindDTO{
    @ApiModelProperty(value = "查询内容", example = "")
    private String searchText = ""; //查询内容
    @ApiModelProperty(value = "地图目录树对应的id 不传入的话默认查询所有分类", example = "f8665a7f-e66e-4cf0-9ef2-845f6ac3eb7f")
    private String mapCLSId; //地图分类
    @ApiModelProperty(value = "按属性查询", example = "name")
    private String curQueryField; //属性分类
}
