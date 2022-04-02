package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Data
public class MapItemCLSDTO {
    @ApiModelProperty(value = "当前目录中文名", example = "专题制图")
    String currentName = "专题制图"; //当前目录中文名
    @ApiModelProperty(value = "子集目录id列表")
    List<String> childrenId = new ArrayList<>(); //子集目录id列表
    // @ApiModelProperty(value = "父级目录中文名", example = "地图制图")
    // String parentName = "地图制图";

}
