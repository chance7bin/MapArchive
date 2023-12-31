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
    @ApiModelProperty(value = "地图目录树对应的id", example = "87086982-5ab1-473e-a65c-c010958f3ef3")
    String mapCLSId; //地图分类
    @ApiModelProperty(value = "对地图的描述", example = "地形图")
    String mapType; // 同一类型地图明细备注，例如基本比例尺中（地形图、航空图、联合作战图），其他图暂时为“”

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

    @ApiModelProperty(value = "资源读取服务器，servername对应dataServer.xml文件里<server>下面的<name>", example = "localhost")
    String servername;
}
