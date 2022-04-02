package com.opengms.maparchivebackendprj.entity.dto.mapItem;

import com.opengms.maparchivebackendprj.entity.dto.SpecificFindDTO;
import lombok.Data;

import java.util.List;

/**空间查询DTO
 * @Description
 * @Author bin
 * @Date 2021/12/10
 */
@Data
public class SpatialDTO {

    List<List<Double>> pointList;

    SpecificFindDTO findDTO;

}
