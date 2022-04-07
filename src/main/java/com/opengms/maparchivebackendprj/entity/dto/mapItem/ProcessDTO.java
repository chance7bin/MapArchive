package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2021/11/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDTO {

    @ApiModelProperty(value = "需处理的文件路径", example = "D:/mapArchiveFiles/地图参考")
    String processingPath;
//    @ApiModelProperty(value = "保存的文件夹路径", example = "D:/mapArchiveFiles/repository/mapItem")
//    String savePath; //保存路径根据分类自动生成
    @ApiModelProperty(value = "地图目录树对应的id", example = "87086982-5ab1-473e-a65c-c010958f3ef3")
    String mapCLSId; //地图分类
    // @ApiModelProperty(value = "分类id", example = "67320f65-8f84-4588-a3cc-c1804101c34a")
    // String clsId;

    @ApiModelProperty(value = "元数据Excel路径(可选,未填该参数则默认到数据库查)", example = "D:/mapArchiveFiles/DEMO.xls")
    String metadataExcelPath;
    
    // String author;

    @ApiModelProperty(value = "是否关联元数据")
    boolean matchMetadata;
    @ApiModelProperty(value = "是否计算地理坐标")
    boolean calcGeoInfo;
    @ApiModelProperty(value = "是否生成缩略图")
    boolean generateThumbnail;
    @ApiModelProperty(value = "是否进行切图")
    boolean generateTiles;


}
