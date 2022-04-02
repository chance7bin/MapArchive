package com.opengms.maparchivebackendprj.entity.bo.mapItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

/**
 * @Description 地图的几何信息
 * @Author bin
 * @Date 2022/03/01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoInfo {
    GeoJsonPoint center;  // 图像中心点
    // Box box; // 图像box范围
    GeoJsonPolygon polygon; // 图像box范围
}
