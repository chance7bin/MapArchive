package com.opengms.maparchivebackendprj.entity.bo.mapItem;

import lombok.Data;

/**
 * @Description 图片存放地址
 * @Author bin
 * @Date 2021/10/08
 */
@Data
public class ImageUrl {
    String originalUrl;  //原始图片
    String thumbnailUrl;  //缩略图
    String tilesDir;  //切片目录
}
