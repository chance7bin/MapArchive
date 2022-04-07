package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.dao.IMetadataTableDao;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.GeoInfo;
import com.opengms.maparchivebackendprj.entity.bo.mapItem.ScaleCoordinate;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import com.opengms.maparchivebackendprj.entity.po.MetadataTable;
import com.opengms.maparchivebackendprj.service.IGeoInfoService;
import com.sun.javafx.scene.transform.TransformUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

import static com.opengms.maparchivebackendprj.entity.enums.MapClassification.BASIC_SCALE_MAP_TEN;

/**
 * @Description 地理坐标计算
 * @Author bin
 * @Date 2022/03/24
 */
@Service
public class GeoInfoServiceImpl implements IGeoInfoService {

    @Autowired
    IMetadataTableDao metadataTableDao;

    @Override
    public GeoInfo getCoordinate(String filename, String mapCLSId) {
        GeoInfo coordinate = null;

        // 得到文件名（去掉后缀）
        // filename = filename.substring(0, filename.lastIndexOf("."));

        // 合辐的情况不做处理
        if (filename.contains("合")){
            return null;
        }

        MetadataTable metadataTable = metadataTableDao.findById(mapCLSId);
        String collection = metadataTable.getCollection();

        switch (collection){
            case "BASIC_SCALE_MAP_TEN":{
                coordinate = getCoordinate10w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_ONE":{
                coordinate = getCoordinate1w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_TWO_DOT_FIVE":{
                coordinate = getCoordinate2Dot5w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_FIVE":{
                coordinate = getCoordinate5w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_TWENTY":{
                coordinate = getCoordinate20w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_TWENTY_FIVE":{
                coordinate = getCoordinate25w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_FIFTY":{
                coordinate = getCoordinate50w(filename);
                break;
            }
            case "BASIC_SCALE_MAP_HUNDRED":{
                coordinate = getCoordinate100w(filename);
                break;
            }
            default:{
                // GeoInfo coordinate10w = null;
            }
        }


        return coordinate;
    }


    /**
     * 根据文件名得到1:10w图幅的地理坐标
     * @param filename
     * @return com.example.maparchivebackend.entity.bo.ScaleCoordinate
     * @Author bin
     **/
    private GeoInfo getCoordinate10w(String filename){

        //// 对文件名做处理
        //// filename = filename.replace("(",".").replace(")","").replace(",",".");
        //
        //// 切分得到 X - Y - Z
        //String[] split = filename.split("-");
        //
        //// 不做特殊情况的处理
        //if (split.length > 3) {
        //    return null;
        //}
        //
        //// 得到Z及后面的数字
        //String[] split1 = split[2].split("\\.");
        //// 如果数组长度为1或者为2，取第0个
        //// 如果数组长度为3，取第0，1个
        //
        //// List<String> zList = new ArrayList<>();
        //ScaleCoordinate coordinate = new ScaleCoordinate();
        //if (split1.length <= 2){
        //    coordinate = calc10w(Double.parseDouble(split[0]), Double.parseDouble(split[1]), split1[0]);
        //}else {
        //    ScaleCoordinate coordinate1 = calc10w(Double.parseDouble(split[0]), Double.parseDouble(split[1]), split1[0]);
        //    ScaleCoordinate coordinate2 = calc10w(Double.parseDouble(split[0]), Double.parseDouble(split[1]), split1[1]);
        //    coordinate = get2MapCoordinate(coordinate1, coordinate2);
        //}

        // new -----------------------------------------------------

        // X-Y-Z (Z: 001-144)
        // double X, double Y, String z

        //01-47-060.1965
        //01-48-027.028.1965
        //02-47-005.1972.1954年北京坐标系
        //02-47-006.018.1972.1954年北京坐标系
        //13-50-141.12-50-009.1968

        String[] split = filename.split("\\.");

        ScaleCoordinate coordinate = new ScaleCoordinate();
        if (split.length == 2){
            //01-47-060.1965
            String[] split1 = split[0].split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split1[2];
            coordinate = calc10w(X, Y, z);


        } else if (split.length == 3){

            if (split[1].length() == 3){
                //01-48-027.028.1965
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                String z1 = split1[2];
                String z2 = split[1];
                ScaleCoordinate coordinate1 = calc10w(X, Y, z1);
                ScaleCoordinate coordinate2 = calc10w(X, Y, z2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);


            } else if (split[1].length() == 4){
                //02-47-005.1972.1954年北京坐标系
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                String z = split1[2];
                coordinate = calc10w(X, Y, z);

            } else {
                //13-50-141.12-50-009.1968
                String[] split0 = split[0].split("-");
                String[] split1 = split[1].split("-");
                double X1 = Double.valueOf(split0[0]);
                double Y1 = Double.valueOf(split0[1]);
                String z1 = split0[2];
                double X2 = Double.valueOf(split1[0]);
                double Y2 = Double.valueOf(split1[1]);
                String z2 = split1[2];
                ScaleCoordinate coordinate1 = calc10w(X1, Y1, z1);
                ScaleCoordinate coordinate2 = calc10w(X2, Y2, z2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);

            }

        } else if (split.length == 4){
            //02-47-006.018.1972.1954年北京坐标系
            String[] split1 = split[0].split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z1 = split1[2];
            String z2 = split[1];
            ScaleCoordinate coordinate1 = calc10w(X, Y, z1);
            ScaleCoordinate coordinate2 = calc10w(X, Y, z2);
            coordinate = get2MapCoordinate(coordinate1,coordinate2);

        } else {
            return null;
        }

        return getGeoInfo(coordinate);
    }

    private ScaleCoordinate get2MapCoordinate(ScaleCoordinate coordinate1, ScaleCoordinate coordinate2){
        ScaleCoordinate coordinate = new ScaleCoordinate();
        coordinate.setLeft_Bottom_Lat(Math.min(coordinate1.getLeft_Bottom_Lat(), coordinate2.getLeft_Bottom_Lat()));
        coordinate.setLeft_Bottom_Lon(Math.min(coordinate1.getLeft_Bottom_Lon(),coordinate2.getLeft_Bottom_Lon()));
        coordinate.setRight_Upper_Lat(Math.max(coordinate1.getRight_Upper_Lat(),coordinate2.getRight_Upper_Lat()));
        coordinate.setRight_Upper_Lon(Math.max(coordinate1.getRight_Upper_Lon(),coordinate2.getRight_Upper_Lon()));
        return coordinate;
    }

    private ScaleCoordinate get4MapCoordinate(
        ScaleCoordinate coordinate1, ScaleCoordinate coordinate2, ScaleCoordinate coordinate3, ScaleCoordinate coordinate4){

        ScaleCoordinate coordinate = new ScaleCoordinate();

        coordinate.setLeft_Bottom_Lat(
            Collections.min(
                Arrays.asList(
                    coordinate1.getLeft_Bottom_Lat(),
                    coordinate2.getLeft_Bottom_Lat(),
                    coordinate3.getLeft_Bottom_Lat(),
                    coordinate4.getLeft_Bottom_Lat())));
        coordinate.setLeft_Bottom_Lon(
            Collections.min(
                Arrays.asList(
                    coordinate1.getLeft_Bottom_Lon(),
                    coordinate2.getLeft_Bottom_Lon(),
                    coordinate3.getLeft_Bottom_Lon(),
                    coordinate4.getLeft_Bottom_Lon())));
        coordinate.setRight_Upper_Lat(
            Collections.max(
                Arrays.asList(
                    coordinate1.getRight_Upper_Lat(),
                    coordinate2.getRight_Upper_Lat(),
                    coordinate3.getRight_Upper_Lat(),
                    coordinate4.getRight_Upper_Lat())));
        coordinate.setRight_Upper_Lon(
            Collections.max(
                Arrays.asList(
                    coordinate1.getRight_Upper_Lon(),
                    coordinate2.getRight_Upper_Lon(),
                    coordinate3.getRight_Upper_Lon(),
                    coordinate4.getRight_Upper_Lon())));

        return coordinate;
    }


    private GeoInfo getGeoInfo(ScaleCoordinate coordinate){
        double left_bottom_lat = coordinate.getLeft_Bottom_Lat();
        double left_bottom_lon = coordinate.getLeft_Bottom_Lon();
        double right_upper_lat = coordinate.getRight_Upper_Lat();
        double right_upper_lon = coordinate.getRight_Upper_Lon();

        double lon = (left_bottom_lon + right_upper_lon) / 2;
        double lat = (left_bottom_lat + right_upper_lat) / 2;
        GeoJsonPoint center = new GeoJsonPoint(lon, lat);

        // Box box = new Box(
        //     new Point(left_bottom_lon, left_bottom_lat),
        //     new Point(right_upper_lon, right_upper_lat)
        // );


        GeoJsonPolygon polygon = new GeoJsonPolygon(
            new Point(left_bottom_lon, left_bottom_lat),
            new Point(left_bottom_lon, right_upper_lat),
            new Point(right_upper_lon, right_upper_lat),
            new Point(right_upper_lon, left_bottom_lat),
            new Point(left_bottom_lon, left_bottom_lat)
        );

        GeoInfo geoInfo = new GeoInfo(center,polygon);

        return geoInfo;
    }


    private GeoInfo getCoordinate1w(String filename){

        // 05-49-005-A-1-(2).06-49-137-C-3-(4).1978.1954年北京坐标系
        // 06-49-004-A-2-(2).2011.2000中国大地坐标系
        // 05-49-040-B-2-(2).0.1954年北京坐标系
        // 05-49-040-A-2-(1).0.
        // 10-50-131-A-1-(4).2-(3).3-(2).4-(1).1966.1954年北京坐标系
        // 万山地区15.1963.1954年北京坐标系
        // 上海市23.1965.
        // Ｘ-32-Б.1958.1954年北京坐标系
        // ＸⅢ-07.0.
        // String filename = "05-49-005-A-1-(2).06-49-137-C-3-(4).1978.1954年北京坐标系";
        // String filename = "10-50-131-A-1-(4).2-(3).3-(2).4-(1).1966.1954年北京坐标系";
        // String filename = "05-49-040-B-2-(2).0.1954年北京坐标系";
        String[] split = filename.split("\\(");
        // System.out.println(split);

        ScaleCoordinate coordinate = new ScaleCoordinate();
        // double X, double Y, String z, char w, String u, String v
        if (split.length == 2){
            // 06-49-004-A-2-(2).2011.2000中国大地坐标系
            // 05-49-040-B-2-(2).0.1954年北京坐标系
            // 05-49-040-A-2-(1).0.
            String[] split1 = filename.split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split1[2];
            char w = split1[3].toCharArray()[0];
            String u = split1[4];
            String v = split1[5].substring(1,2);
            coordinate = calc1w(X,Y,z,w,u,v);
        } else if (split.length == 3){
            // 05-49-005-A-1-(2).06-49-137-C-3-(4).1978.1954年北京坐标系
            String[] coo = filename.split("\\.");
            String c1 = coo[0];
            String c2 = coo[1];
            String[] split1 = c1.split("-");
            double X1 = Double.valueOf(split1[0]);
            double Y1 = Double.valueOf(split1[1]);
            String z1 = split1[2];
            char w1 = split1[3].toCharArray()[0];
            String u1 = split1[4];
            String v1 = split1[5].substring(1,2);
            ScaleCoordinate coordinate1 = calc1w(X1,Y1,z1,w1,u1,v1);
            String[] split2 = c2.split("-");
            double X2 = Double.valueOf(split2[0]);
            double Y2 = Double.valueOf(split2[1]);
            String z2 = split2[2];
            char w2 = split2[3].toCharArray()[0];
            String u2 = split2[4];
            String v2 = split2[5].substring(1,2);
            ScaleCoordinate coordinate2 = calc1w(X2,Y2,z2,w2,u2,v2);
            coordinate = get2MapCoordinate(coordinate1,coordinate2);

        } else if (split.length == 5){
            // 10-50-131-A-1-(4).2-(3).3-(2).4-(1).1966.1954年北京坐标系
            String[] split1 = filename.split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split1[2];
            char w = split1[3].toCharArray()[0];
            String u1 = split[0].substring(split[0].length()-2,split[0].length()-1);
            String u2 = split[1].substring(split[1].length()-2,split[1].length()-1);
            String u3 = split[2].substring(split[2].length()-2,split[2].length()-1);
            String u4 = split[3].substring(split[3].length()-2,split[3].length()-1);
            String v1 = split[1].substring(0,1);
            String v2 = split[2].substring(0,1);
            String v3 = split[3].substring(0,1);
            String v4 = split[4].substring(0,1);
            ScaleCoordinate coordinate1 = calc1w(X,Y,z,w,u1,v1);
            ScaleCoordinate coordinate2 = calc1w(X,Y,z,w,u2,v2);
            ScaleCoordinate coordinate3 = calc1w(X,Y,z,w,u3,v3);
            ScaleCoordinate coordinate4 = calc1w(X,Y,z,w,u4,v4);
            coordinate = get4MapCoordinate(coordinate1,coordinate2,coordinate3,coordinate4);

        } else {
            // 万山地区15.1963.1954年北京坐标系
            // 上海市23.1965.
            // Ｘ-32-Б.1958.1954年北京坐标系
            // ＸⅢ-07.0.
            return null;
        }

        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate2Dot5w(String filename){

        // X-Y-Z-W-U (Z: 001-144; W: A, B, C, D; U: 1-4)
        // 08-51-053-A-1
        // 08-51-053-A-1.3
        // 08-51-053-D-1.C-2
        // 13-50-091-B-2.079-D-4
        // 14-51-109-C-1.14-50-120-D-2
        // 旌德县(4)

        // String filename = "14-51-109-C-1.14-50-120-D-2";
        // String filename = "08-51-053-A-1";

        String[] split = filename.split("\\.");
        // System.out.println(split);

        ScaleCoordinate coordinate = new ScaleCoordinate();

        if (split.length == 1){

            String[] split1 = filename.split("-");

            if (split1.length == 1){
                // 旌德县(4)
                return null;
            } else {
                // 08-51-053-A-1
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                String z = split1[2];
                char w = split1[3].toCharArray()[0];
                String u = split1[4];
                coordinate = calc2Dot5w(X, Y, z, w, u);

            }


        } else if (split.length == 2){

            String[] split0 = split[0].split("-");
            String[] split1 = split[1].split("-");

            if (split1.length == 1){
                // 08-51-053-A-1.3
                double X = Double.valueOf(split0[0]);
                double Y = Double.valueOf(split0[1]);
                String z = split0[2];
                char w = split0[3].toCharArray()[0];
                String u1 = split0[4];
                String u2 = split1[0];

                ScaleCoordinate coordinate1 = calc2Dot5w(X, Y, z, w, u1);
                ScaleCoordinate coordinate2 = calc2Dot5w(X, Y, z, w, u2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);

            } else if (split1.length == 2){
                // 08-51-053-D-1.C-2
                double X = Double.valueOf(split0[0]);
                double Y = Double.valueOf(split0[1]);
                String z = split0[2];
                char w1 = split0[3].toCharArray()[0];
                char w2 = split1[0].toCharArray()[0];
                String u1 = split0[4];
                String u2 = split1[1];
                ScaleCoordinate coordinate1 = calc2Dot5w(X, Y, z, w1, u1);
                ScaleCoordinate coordinate2 = calc2Dot5w(X, Y, z, w2, u2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);

            } else if (split1.length == 3){
                // 13-50-091-B-2.079-D-4
                double X = Double.valueOf(split0[0]);
                double Y = Double.valueOf(split0[1]);
                String z1 = split0[2];
                String z2 = split1[0];
                char w1 = split0[3].toCharArray()[0];
                char w2 = split1[1].toCharArray()[0];
                String u1 = split0[4];
                String u2 = split1[2];
                ScaleCoordinate coordinate1 = calc2Dot5w(X, Y, z1, w1, u1);
                ScaleCoordinate coordinate2 = calc2Dot5w(X, Y, z2, w2, u2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);


            } else if (split1.length == 5){
                // 14-51-109-C-1.14-50-120-D-2
                double X1 = Double.valueOf(split0[0]);
                double X2 = Double.valueOf(split1[0]);
                double Y1 = Double.valueOf(split0[1]);
                double Y2 = Double.valueOf(split1[1]);
                String z1 = split0[2];
                String z2 = split1[2];
                char w1 = split0[3].toCharArray()[0];
                char w2 = split1[3].toCharArray()[0];
                String u1 = split0[4];
                String u2 = split1[4];
                ScaleCoordinate coordinate1 = calc2Dot5w(X1, Y1, z1, w1, u1);
                ScaleCoordinate coordinate2 = calc2Dot5w(X2, Y2, z2, w2, u2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);


            } else {
                return null;
            }


        } else {
            return null;
        }


        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate5w(String filename){

        // X-Y-Z-W (Z: 001-144; W: A, B, C, D)
        // 06-49-100-B.1958
        // 06-49-100-B.A.1980
        // 08-46-099-D.1968.1960年拉萨坐标系
        // 09-46-078-A.2013.2000中国大地坐标系
        // 11-52-066-C.078-A.1990.1954年北京坐标系
        // 12-50-009-A 与13-50-141-C合幅.1966.1954年北京坐标系

        String[] split = filename.split("\\.");

        ScaleCoordinate coordinate = new ScaleCoordinate();

        if (split.length == 2){
            // 06-49-100-B.1958
            String[] split1 = split[0].split("-");
            // double X, double Y, String z, char w
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split1[2];
            char w = split1[3].toCharArray()[0];
            coordinate = calc5w(X, Y, z, w);
        } else if (split.length == 3){

            if (split[1].length() == 1){
                // 06-49-100-B.A.1980
                String[] split0 = split[0].split("-");
                String split1 = split[1];
                double X = Double.valueOf(split0[0]);
                double Y = Double.valueOf(split0[1]);
                String z = split0[2];
                char w1 = split0[3].toCharArray()[0];
                char w2 = split1.toCharArray()[0];
                ScaleCoordinate coordinate1 = calc5w(X, Y, z, w1);
                ScaleCoordinate coordinate2 = calc5w(X, Y, z, w2);
                coordinate = get2MapCoordinate(coordinate1,coordinate2);


            } else {
                // 08-46-099-D.1968.1960年拉萨坐标系
                // 09-46-078-A.2013.2000中国大地坐标系
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                String z = split1[2];
                char w = split1[3].toCharArray()[0];
                coordinate = calc5w(X, Y, z, w);
            }


        } else if (split.length == 4){
            // 11-52-066-C.078-A.1990.1954年北京坐标系
            String[] split0 = split[0].split("-");
            String[] split1 = split[1].split("-");
            double X = Double.valueOf(split0[0]);
            double Y = Double.valueOf(split0[1]);
            String z1 = split0[2];
            String z2 = split1[0];
            char w1 = split0[3].toCharArray()[0];
            char w2 = split1[1].toCharArray()[0];
            ScaleCoordinate coordinate1 = calc5w(X, Y, z1, w1);
            ScaleCoordinate coordinate2 = calc5w(X, Y, z2, w2);
            coordinate = get2MapCoordinate(coordinate1,coordinate2);

        } else {
            return null;
        }

        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate20w(String filename){

        // X-Y-(Z) (Z: 01-36)
        // double X, double Y, String z
        // 02-47-(10).1965.
        // 02-47-(05).1974.1954年北京坐标系
        // 03-47-(09).(08).1965.
        // 03-47-(21).(20).1974.1954年北京坐标系
        // 03-49-(13).(14).(19).(20).1959.

        String[] split = filename.split("\\(");

        ScaleCoordinate coordinate = new ScaleCoordinate();
        if (split.length == 2){
            // 02-47-(10).1965.
            // 02-47-(05).1974.1954年北京坐标系
            String[] split1 = filename.split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split[1].substring(0, 2);
            coordinate = calc20w(X, Y, z);
            System.out.println();

        } else if (split.length == 3){
            // 03-47-(09).(08).1965.
            // 03-47-(21).(20).1974.1954年北京坐标系
            String[] split1 = filename.split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z1 = split[1].substring(0, 2);
            String z2 = split[2].substring(0, 2);
            ScaleCoordinate coordinate1 = calc20w(X, Y, z1);
            ScaleCoordinate coordinate2 = calc20w(X, Y, z2);
            coordinate = get2MapCoordinate(coordinate1,coordinate2);

        } else if (split.length == 5){
            // 03-49-(13).(14).(19).(20).1959.
            String[] split1 = filename.split("-");
            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z1 = split[1].substring(0, 2);
            String z2 = split[2].substring(0, 2);
            String z3 = split[3].substring(0, 2);
            String z4 = split[4].substring(0, 2);
            ScaleCoordinate coordinate1 = calc20w(X, Y, z1);
            ScaleCoordinate coordinate2 = calc20w(X, Y, z2);
            ScaleCoordinate coordinate3 = calc20w(X, Y, z3);
            ScaleCoordinate coordinate4 = calc20w(X, Y, z4);
            coordinate = get4MapCoordinate(coordinate1,coordinate2,coordinate3,coordinate4);

        } else {

            return null;
        }

        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate25w(String filename){

        // X-Y-[Z] (Z: 01-16)
        // double X, double Y, String z

        // 01-49-[04].1991
        // 02-35-[12].2015.2000中国大地坐标系
        // 08-51-[06].[07].1987.1954年北京坐标系


        String[] split = filename.split("\\[");

        ScaleCoordinate coordinate = new ScaleCoordinate();
        if (split.length == 2){
            // 01-49-[04].1991
            // 02-35-[12].2015.2000中国大地坐标系

            String[] split1 = split[0].split("-");

            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z = split[1].substring(0, 2);

            coordinate = calc25w(X, Y, z);

        } else if (split.length == 3){
            // 08-51-[06].[07].1987.1954年北京坐标系
            String[] split1 = split[0].split("-");

            double X = Double.valueOf(split1[0]);
            double Y = Double.valueOf(split1[1]);
            String z1 = split[1].substring(0, 2);
            String z2 = split[2].substring(0, 2);

            ScaleCoordinate coordinate1 = calc25w(X, Y, z1);
            ScaleCoordinate coordinate2 = calc25w(X, Y, z2);
            coordinate = get2MapCoordinate(coordinate1,coordinate2);
        } else {

            return null;

        }

        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate50w(String filename){

        // X-Y-Z (Z: A, B, C, D)
        // double X, double Y, char z

        // 01-49-A.2008.
        // 02-49-A.1992.1954年北京坐标系

        String[] split = filename.split("-");
        double X = Double.valueOf(split[0]);
        double Y = Double.valueOf(split[1]);
        char z = split[2].toCharArray()[0];

        ScaleCoordinate coordinate = calc50w(X, Y, z);

        return getGeoInfo(coordinate);

    }

    private GeoInfo getCoordinate100w(String filename){

        // X-Y-Z
        // double X, double Y


        // 01-04.1961.    ✔
        // 01-15.16.1962.    ✔
        // 01-47.2014.2000中国大地坐标系    ✔
        // 05-26.27、04-26.27.1964.
        // 12-21.11-21.1962.
        // 13-(41)42.2006.
        // 13-(44)45.2009.2000中国大地坐标系
        // 13-40(41).2006.
        // 20-09.10.11.12.1961.      ✔

        // S 在南半球 维度取负
        // S01-02.1961.    ✔
        // S02-42.43.1964.    ✔
        // S01-51.2014.2000中国大地坐标系    ✔
        // S04-47.S03-47.1962.
        // S10-28.29.S11-28.29.1964.

        ScaleCoordinate coordinate = new ScaleCoordinate();
        char start = filename.charAt(0);
        if (start == 'S'){

            filename = filename.substring(1);

            String[] split = filename.split("\\.");

            if (split.length == 2){
                // S01-02.1961.    ✔
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                coordinate = calc100wS(X, Y);
                System.out.println();

            } else if (split.length == 3){
                if (split[1].length() == 2){
                    // S02-42.43.1964.    ✔
                    String[] split1 = split[0].split("-");
                    double X = Double.valueOf(split1[0]);
                    double Y1 = Double.valueOf(split1[1]);
                    double Y2 = Double.valueOf(split[1]);
                    ScaleCoordinate coordinate1 = calc100wS(X, Y1);
                    ScaleCoordinate coordinate2 = calc100wS(X, Y2);
                    coordinate = get2MapCoordinate(coordinate1,coordinate2);
                    System.out.println();
                } else if (split[1].length() == 4){
                    // S01-51.2014.2000中国大地坐标系    ✔
                    String[] split1 = split[0].split("-");
                    double X = Double.valueOf(split1[0]);
                    double Y = Double.valueOf(split1[1]);
                    coordinate = calc100wS(X, Y);
                    System.out.println();
                } else {
                    // S04-47.S03-47.1962.
                    return null;
                }


            } else {
                // S10-28.29.S11-28.29.1964.
                return null;
            }


        } else {

            String[] split = filename.split("\\.");

            if (split.length == 2){
                // 01-04.1961.    ✔
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y = Double.valueOf(split1[1]);
                coordinate = calc100w(X, Y);
                System.out.println();

            } else if (split.length == 3){
                if (split[1].length() == 2){
                    // 01-15.16.1962.    ✔
                    String[] split1 = split[0].split("-");
                    double X = Double.valueOf(split1[0]);
                    double Y1 = Double.valueOf(split1[1]);
                    double Y2 = Double.valueOf(split[1]);
                    ScaleCoordinate coordinate1 = calc100w(X, Y1);
                    ScaleCoordinate coordinate2 = calc100w(X, Y2);
                    coordinate = get2MapCoordinate(coordinate1,coordinate2);
                    System.out.println();
                } else if (split[1].length() == 4){
                    // 01-47.2014.2000中国大地坐标系    ✔
                    String[] split1 = split[0].split("-");
                    double X = Double.valueOf(split1[0]);
                    double Y = Double.valueOf(split1[1]);
                    coordinate = calc100w(X, Y);
                    System.out.println();
                } else {
                    return null;
                }


            } else if (split.length == 5){
                // 20-09.10.11.12.1961.      ✔
                String[] split1 = split[0].split("-");
                double X = Double.valueOf(split1[0]);
                double Y1 = Double.valueOf(split1[1]);
                double Y2 = Double.valueOf(split[1]);
                double Y3 = Double.valueOf(split[2]);
                double Y4 = Double.valueOf(split[3]);
                ScaleCoordinate coordinate1 = calc100w(X, Y1);
                ScaleCoordinate coordinate2 = calc100w(X, Y2);
                ScaleCoordinate coordinate3 = calc100w(X, Y3);
                ScaleCoordinate coordinate4 = calc100w(X, Y4);
                coordinate = get4MapCoordinate(coordinate1,coordinate2,coordinate3,coordinate4);
                System.out.println();
            } else {
                return null;
            }

        }

        return getGeoInfo(coordinate);

    }


    /**100W**********************/
    // X-Y-Z
    private ScaleCoordinate calc100w(double X, double Y){

        double Left_Bottom_Lon = (Y-1)*6-180;
        double Left_Bottom_Lat = (X-1)*4;

        double Right_Upper_Lon = Y*6-180;
        double Right_Upper_Lat = X*4;

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);
    }

    //南半球
    private ScaleCoordinate calc100wS(double X, double Y){

        double Left_Bottom_Lon = (Y-1)*6-180;
        double Left_Bottom_Lat = (X-1)*4;
        Left_Bottom_Lat = -Left_Bottom_Lat;

        double Right_Upper_Lon = Y*6-180;
        double Right_Upper_Lat = X*4;
        Right_Upper_Lat = -Right_Upper_Lat;

        return new ScaleCoordinate(Left_Bottom_Lon, Right_Upper_Lat, Right_Upper_Lon, Left_Bottom_Lat);
    }



    /**50W**********************/
    // X-Y-Z (Z: A, B, C, D)
    private ScaleCoordinate calc50w(double X, double Y, char z){

        // Z=ord(Z)-65
        double Z = Double.valueOf(z) - 65;
        double Left_Bottom_Lon = (Y-1)*6-180+(Z%2)*(6.0/2);
        double Left_Bottom_Lat = (X-1)*4+(2-Math.floor(Z/2)-1)*(4.0/2);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%2+1)*(6.0/2);
        double Right_Upper_Lat = (X-1)*4+(2-Math.floor(Z/2))*(4.0/2);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }


    /**25W**********************/
    // X-Y-[Z] (Z: 01-16)
    private ScaleCoordinate calc25w(double X, double Y, String z){

        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        double Left_Bottom_Lon = (Y-1)*6-180+(Z%4)*(6.0/4);
        double Left_Bottom_Lat = (X-1)*4+(4-Math.floor(Z/4)-1)*(4.0/4);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%4+1)*(6.0/4);
        double Right_Upper_Lat = (X-1)*4+(4-Math.floor(Z/4))*(4.0/4);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }


    /**20W**********************/
    // X-Y-(Z) (Z: 01-36)
    private ScaleCoordinate calc20w(double X, double Y, String z){

        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        double Left_Bottom_Lon = (Y-1)*6-180+(Z%6)*(6.0/6);
        double Left_Bottom_Lat = (X-1)*4+(6-Math.floor(Z/6)-1)*(4.0/6);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%6+1)*(6.0/6);
        double Right_Upper_Lat = (X-1)*4+(6-Math.floor(Z/6))*(4.0/6);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);
    }


    /**10W**********************/
    // X-Y-Z (Z: 001-144)
    private ScaleCoordinate calc10w(double X, double Y, String z){
        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        double Left_Bottom_Lon = (Y-1)*6-180+(Z%12)*(6.0/12);
        double Left_Bottom_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%12+1)*(6.0/12);
        double Right_Upper_Lat = (X-1)*4+(12-Math.floor(Z/12))*(4.0/12);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }




    /**5W**********************/
    // X-Y-Z-W (Z: 001-144; W: A, B, C, D)
    private ScaleCoordinate calc5w(double X, double Y, String z, char w){

        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        // W=ord(W)-65
        double W = Double.valueOf(w) - 65;
        double Left_Bottom_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2)*(6.0/24);
        double Left_Bottom_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2)-1)*(4.0/24);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2+1)*(6.0/24);
        double Right_Upper_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2))*(4.0/24);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }




    /**2.5W**********************/
    // X-Y-Z-W-U (Z: 001-144; W: A, B, C, D; U: 1-4)
    private ScaleCoordinate calc2Dot5w(double X, double Y, String z, char w, String u){
        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        // W=ord(W)-65
        double W = Double.valueOf(w) - 65;
        // U=U-1
        double U = Double.valueOf(u) - 1;

        double Left_Bottom_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2)*(6.0/24)+(U%2)*(6.0/48);
        double Left_Bottom_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2)-1)*(4.0/24)+(2-Math.floor(U/2)-1)*(4.0/48);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2)*(6.0/24)+(U%2+1)*(6.0/48);
        double Right_Upper_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2)-1)*(4.0/24)+(2-Math.floor(U/2))*(4.0/48);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }





    /**1W**********************/
    // X-Y-Z-W-U-(V) (Z: 001-144; W: A, B, C, D; U: 1-4; V: 1-4)
    private ScaleCoordinate calc1w(double X, double Y, String z, char w, String u, String v){
        // Z=Z-1
        double Z = Double.valueOf(z) - 1;
        // W=ord(W)-65
        double W = Double.valueOf(w) - 65;
        // U=U-1
        double U = Double.valueOf(u) - 1;
        // V=V-1
        double V = Double.valueOf(v) - 1;

        double Left_Bottom_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2)*(6.0/24)+(U%2)*(6.0/48)+(V%2)*(6.0/96);
        double Left_Bottom_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2)-1)*(4.0/24)+(2-Math.floor(U/2)-1)*(4.0/48)+(2-Math.floor(V/2)-1)*(4.0/96);

        double Right_Upper_Lon = (Y-1)*6-180+(Z%12)*(6.0/12)+(W%2)*(6.0/24)+(U%2)*(6.0/48)+(V%2+1)*(6.0/96);
        double Right_Upper_Lat = (X-1)*4+(12-Math.floor(Z/12)-1)*(4.0/12)+(2-Math.floor(W/2)-1)*(4.0/24)+(2-Math.floor(U/2)-1)*(4.0/48)+(2-Math.floor(V/2))*(4.0/96);

        return new ScaleCoordinate(Left_Bottom_Lon, Left_Bottom_Lat, Right_Upper_Lon, Right_Upper_Lat);

    }
    
}
