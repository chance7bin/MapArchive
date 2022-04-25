package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author bin
 * @Date 2021/12/07
 */
@Data
public class MapItemUpdateDTO {

    // Metadata metadata;
    Map<String, Object> metadata;

    List<List<Double>> pointList;

}
