package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.dto.TiandituTilesDTO;
import com.opengms.maparchivebackendprj.entity.dto.TilesDTO;
import com.opengms.maparchivebackendprj.service.ITilesService;
import com.opengms.maparchivebackendprj.utils.FileUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Resource(name="mapboxConnection")
    Connection mapboxConnection;




    @Override
    public void getTiandituTiles(TiandituTilesDTO tilesDTO, HttpServletResponse response) {

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

        queryMbtiles(tilesDTO, connection, response);

    }

    @Override
    public void getMapboxTiles(TilesDTO tilesDTO, HttpServletResponse response) {

        try {
            Statement statement = mapboxConnection.createStatement();
            // 得到结果集
            String sql = "SELECT * FROM tiles WHERE zoom_level = "+ tilesDTO.getZoom_level() +
                " AND tile_column = "+ tilesDTO.getTile_column() +
                " AND tile_row = "+ tilesDTO.getTile_row() ;
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()) {
                byte[] imgByte = (byte[]) rs.getObject("tile_data");
                // 由于mapbox只能加载未压缩的pbf格式数据，
                // 但直使用tippecanoe或mbuitl生成的pbf是经过gzip压缩的数据
                // [不执行解压缩，mapbox加载数据会报：“Unimplemented type: 3” 错误]，
                // 所以需要解压缩
                byte[] bytes = FileUtils.gzipUncompress(imgByte);
                InputStream is = new ByteArrayInputStream(bytes);
                OutputStream os = response.getOutputStream();
                try {
                    int count = 0;
                    byte[] buffer = new byte[1024 * 1024];
                    while ((count = is.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                    os.flush();
                } catch (IOException e) {
                    // e.printStackTrace();
                } finally {
                    os.close();
                    is.close();
                }
            }
            else{
                log.debug("sql: {}",sql);
                log.debug("未找到瓦片!");
            }
            rs.close();
            //statement在每次执行之后都要关了
            statement.close();
        }catch (Exception e){
            // e.printStackTrace();
        }


        // queryMbtiles(tilesDTO, mapboxConnection, response);


    }

    @Override
    public JSONObject getMapboxTilesMetadataJson() {

        JSONObject result = new JSONObject();
        
        try {
            Statement statement = mapboxConnection.createStatement();
            // 得到结果集
            String sql = "SELECT * FROM metadata";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String name = (String) rs.getObject("name");
                String value = (String) rs.getObject("value");

                JSONObject jsonObject = formatMetadata(name, value);

                result.put(jsonObject.getString("name"),jsonObject.get("value"));

            }
            rs.close();
            //statement在每次执行之后都要关了
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        result.put("tiles", Arrays.asList("http://localhost:8999/tiles/mapbox/{z}/{x}/{y}.pbf"));
        // result.put("tiles", Arrays.asList("https://api.maptiler.com/tiles/v3/{z}/{x}/{y}.pbf?key=XAapkmkXQpx839NCfnxD"));

        return result;
    }





    private JSONObject formatMetadata(String name, String value){

        JSONObject res = new JSONObject();


        if (name.equals("json")){
            JSONObject valueObj = JSONObject.parseObject(value);
            JSONArray vector_layers = valueObj.getJSONArray("vector_layers");
            // res.put("vector_layers",vector_layers);
            res.put("name","vector_layers");
            res.put("value",vector_layers);
        } else if (name.equals("minzoom") || name.equals("maxzoom") || name.equals("maskLevel")){
            //整型
            // res.put(name,Integer.parseInt(value));
            res.put("name",name);
            res.put("value",Integer.parseInt(value));
        } else if (name.equals("bounds") || name.equals("center") || name.equals("extent")){
            //数组
            String[] arr = value.split(",");
            List<Double> doubles = new ArrayList<>();
            for (String s : arr) {
                doubles.add(Double.parseDouble(s));
            }
            // res.put(name,arr);
            res.put("name",name);
            res.put("value",doubles);
        } else {
            // res.put(name,value);
            res.put("name",name);
            res.put("value",value);
        }

        return res;

    }


    private void queryMbtiles(TilesDTO tilesDTO, Connection connection, HttpServletResponse response){

        try {
            Statement statement = connection.createStatement();
            // 得到结果集
            String sql = "SELECT * FROM tiles WHERE zoom_level = "+ tilesDTO.getZoom_level() +
                " AND tile_column = "+ tilesDTO.getTile_column() +
                " AND tile_row = "+ tilesDTO.getTile_row() ;
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()) {
                byte[] imgByte = (byte[]) rs.getObject("tile_data");
                // byte[] bytes = FileUtils.gzipUncompress(imgByte);
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
                log.debug("未找到瓦片!");
            }
            rs.close();
            //statement在每次执行之后都要关了
            statement.close();
        }catch (Exception e){
            // e.printStackTrace();
        }


    }


}
