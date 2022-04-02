package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
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
public class MapItemMapDBCLSDTO {
    @ApiModelProperty(value = "当前目录中文名", example = "专题制图")
    String currentName = "专题制图"; //当前目录中文名
    @ApiModelProperty(value = "数据库名字")
    List<MapClassification> childrenName = new ArrayList<>(); //子集目录数据库列表

}
