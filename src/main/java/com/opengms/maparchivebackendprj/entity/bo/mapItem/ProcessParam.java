package com.opengms.maparchivebackendprj.entity.bo.mapItem;

import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 生成缩略图以及进行切片所需的参数
 * @Author bin
 * @Date 2022/03/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessParam {

    String mapItemId; //地图类id
    // String processingItemId; //处理类id
    String inputPath; //待处理的文件路径
    String thumbnailOutputDir; //缩略图输出文件夹
    String tilesOutputDir; //切片生成文件夹
    // MapClassification mapCLS; //地图类型

}
