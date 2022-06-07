package com.opengms.maparchivebackendprj.entity.dto;

import com.opengms.maparchivebackendprj.entity.enums.LayerEnum;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/05/02
 */
@Data
public class TiandituTilesDTO extends TilesDTO{
    LayerEnum tile_Layer;
}
