package com.opengms.maparchivebackendprj.controller;

import com.opengms.maparchivebackendprj.entity.dto.TilesDTO;
import com.opengms.maparchivebackendprj.entity.enums.LayerEnum;
import com.opengms.maparchivebackendprj.service.ITilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Api(tags = "地图瓦片接口")
@RestController
@RequestMapping(value = "/tiles")
@Slf4j
public class TilesController {

    @Autowired
    ITilesService tilesService;


    @ApiOperation(value = "得到瓦片图像" )
    @GetMapping("/{layer}/{z}/{x}/{y}")
    public void getTiandituTiles(
        @ApiParam(name = "layer", value = "加载的图层") @PathVariable LayerEnum layer,
        @ApiParam(name = "z", value = "zoom_level") @PathVariable int z,
        @ApiParam(name = "x", value = "tile_column") @PathVariable int x,
        @ApiParam(name = "y", value = "tile_row") @PathVariable int y ,
        HttpServletResponse response){


        // int zoom_level = z;
        // int tile_column = x;
        // int tile_row = y;

        TilesDTO tilesDTO = new TilesDTO();
        tilesDTO.setTile_column(x);
        tilesDTO.setTile_row(y);
        tilesDTO.setZoom_level(z);
        tilesDTO.setTile_Layer(layer);

        tilesService.getTiandituTiles(tilesDTO, response);

    }

}
