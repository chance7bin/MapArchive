package com.opengms.maparchivebackendprj.entity.bo.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2021/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoInfoMeta {
    String city;
    String countryCode;
    String latitude;
    String countryName;
    String region;
    String longitude;
}
