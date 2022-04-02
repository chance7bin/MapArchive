package com.opengms.maparchivebackendprj.entity.dto;

import com.opengms.maparchivebackendprj.entity.enums.LayerEnum;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2021/12/09
 */
@Data
public class TilesDTO {
    int zoom_level;
    int tile_column;
    int tile_row;
    LayerEnum tile_Layer;
}
