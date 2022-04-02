package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.FileInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/09
 */
@Data
public class MapItemAddDTO {

    List<FileInfo> fileInfoList; //上传的文件改成这个字段
    // MapClassification mapCLS; //地图类型
    String mapCLSId; //地图分类

    @ApiModelProperty(value = "元数据Excel路径(可选,未填该参数则默认到数据库查)", example = "D:/mapArchiveFiles/DEMO.xls")
    String metadataTable;

    @ApiModelProperty(value = "是否关联元数据")
    boolean matchMetadata;
    @ApiModelProperty(value = "是否计算地理坐标")
    boolean calcGeoInfo;
    @ApiModelProperty(value = "是否生成缩略图")
    boolean generateThumbnail;
    @ApiModelProperty(value = "是否进行切图")
    boolean generateTiles;
}
