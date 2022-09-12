package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Data
public class BatchProcessDTO {

    @ApiModelProperty(value = "是否关联元数据")
    boolean matchMetadata;
    @ApiModelProperty(value = "是否计算地理坐标")
    boolean calcGeoInfo;
    @ApiModelProperty(value = "是否生成缩略图")
    boolean generateThumbnail;
    @ApiModelProperty(value = "是否进行切图")
    boolean generateTiles;
    @ApiModelProperty(value = "处理的数量")
    int processCount;
    @ApiModelProperty(value = "需要处理的元数据表")
    String mapCLSId;


}
