package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.entity.dto.TilesDTO;
import com.opengms.maparchivebackendprj.service.ITilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/25
 */
@Service
@Slf4j
public class TilesServiceImpl implements ITilesService {

    @Resource(name="imgConnection")
    Connection imgConnection;

    @Resource(name="cvaConnection")
    Connection cvaConnection;

    @Resource(name="ciaConnection")
    Connection ciaConnection;

    @Resource(name="vecConnection")
    Connection vecConnection;

    @Override
    public void getTiandituTiles(TilesDTO tilesDTO, HttpServletResponse response) {

        Connection connection;

        switch (tilesDTO.getTile_Layer()){
            case img_c:{
                connection = imgConnection;
                break;
            }
            case cva_c:{
                connection = cvaConnection;
                break;
            }
            case cia_c:{
                connection = ciaConnection;
                break;
            }
            case vec_c:{
                connection = vecConnection;
                break;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + tilesDTO.getTile_Layer());
        }

        try {
            Statement statement = connection.createStatement();
            // 得到结果集
            String sql = "SELECT * FROM tiles WHERE zoom_level = "+ tilesDTO.getZoom_level() +
                " AND tile_column = "+ tilesDTO.getTile_column() +
                " AND tile_row = "+ tilesDTO.getTile_row() ;
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()) {
                byte[] imgByte = (byte[]) rs.getObject("tile_data");
                InputStream is = new ByteArrayInputStream(imgByte);
                OutputStream os = response.getOutputStream();
                try {
                    int count = 0;
                    byte[] buffer = new byte[1024 * 1024];
                    while ((count = is.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    os.close();
                    is.close();
                }
            }
            else{
                log.debug("sql: {}",sql);
                log.debug("未找到天地图瓦片!");
            }
            rs.close();
            //statement在每次执行之后都要关了
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
