package com.opengms.maparchivebackendprj.entity.bo.mapItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description 图片的元数据
 * @Author bin
 * @Date 2022/03/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageMetadata {

    String name;
    double size; //单位:MB
    int width; //单位:pixels
    int height; //单位:pixels
    Date modifiedData;

}
