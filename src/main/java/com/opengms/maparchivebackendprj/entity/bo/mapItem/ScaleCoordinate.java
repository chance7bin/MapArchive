package com.opengms.maparchivebackendprj.entity.bo.mapItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 自定义坐标对象
 * @Author bin
 * @Date 2022/02/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleCoordinate {

    //左下角经纬度
    double Left_Bottom_Lon;
    double Left_Bottom_Lat;

    //右上角经纬度
    double Right_Upper_Lon;
    double Right_Upper_Lat;

}
