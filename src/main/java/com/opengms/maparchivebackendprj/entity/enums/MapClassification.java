package com.opengms.maparchivebackendprj.entity.enums;

import lombok.AllArgsConstructor;

/**
 * @Description 地图分类 根节点目录
 * @Author bin
 * @Date 2022/02/28
 */
@AllArgsConstructor
public enum MapClassification {

    //一级目录分类
    // BASIC_SCALE_MAP(8,"BASIC_SCALE_MAP","基础比例尺地图"),
    // HISTORY_MAP(9,"HISTORY_MAP","历史地图"),
    // FOREIGN_MAP(10,"FOREIGN_MAP","国外地图"),


    //BasicScaleMap的分类
    BASIC_SCALE_MAP_ONE("BASIC_SCALE_MAP_ONE","1比1万地形图"),
    BASIC_SCALE_MAP_TWO_DOT_FIVE("BASIC_SCALE_MAP_TWO_DOT_FIVE","1比25000地形图"),
    BASIC_SCALE_MAP_FIVE( "BASIC_SCALE_MAP_FIVE","1比5万地形图"),
    BASIC_SCALE_MAP_TEN("BASIC_SCALE_MAP_TEN","1比10万地形图"),
    BASIC_SCALE_MAP_TWENTY( "BASIC_SCALE_MAP_TWENTY","1比20万地形图"),
    BASIC_SCALE_MAP_TWENTY_FIVE( "BASIC_SCALE_MAP_TWENTY_FIVE","1比25万地形图"),
    BASIC_SCALE_MAP_FIFTY("BASIC_SCALE_MAP_FIFTY","1比50万地形图"),
    BASIC_SCALE_MAP_HUNDRED( "BASIC_SCALE_MAP_HUNDRED","1比100万地形图"),


    //...
    //大地测量
    ASTRONOMICAL_MEASURE("ASTRONOMICAL_MEASURE","天文测量"),
    GRAVITY_MEASURE("GRAVITY_MEASURE","重力测量"),
    TRIANGLE_MEASURE("TRIANGLE_MEASURE","三角（导线）测量"),
    LEVEL_MEASURE("LEVEL_MEASURE","水准测量"),
    SPACIAL_MEASURE("SPACIAL_MEASURE","空间大地测量"),

    //专题制图
    THEMATIC_FOREIGN("THEMATIC_FOREIGN","国外地区图"),
    THEMATIC_TAIWAN_RECORD("THEMATIC_TAIWAN_RECORD","档案"),
    THEMATIC_TAIWAN_TOPOGRAPHIC ("THEMATIC_TAIWAN_TOPOGRAPHIC","地形图"),
    THEMATIC_TAIWAN_REFERENCE("THEMATIC_TAIWAN_REFERENCE","地图集_参考资料"),
    THEMATIC_GANGAO_RECORD("THEMATIC_GANGAO_RECORD","档案"),
    THEMATIC_GANGAO_TOPOGRAPHIC("THEMATIC_GANGAO_TOPOGRAPHIC","地形图"),
    THEMATIC_GANGAO_REFERENCE("THEMATIC_GANGAO_REFERENCE","地图集_参考资料"),
    THEMATIC_CITY("THEMATIC_CITY","城市系列图"),
    THEMATIC_TRAFFIC("THEMATIC_TRAFFIC","图集_挂图_交通图"),
    THEMATIC_ISLAND("THEMATIC_ISLAND","岛屿图_地区图"),
    THEMATIC_SPECIAL_TOPIC("THEMATIC_SPECIAL_TOPIC","专题专项"),

    //国外测绘



    ;

    private String nameEn;
    private String nameCn;


    public String getNameEn() {
        return nameEn;
    }

    public String getNameCn() {
        return nameCn;
    }


    public static MapClassification getMapCLSByNameEn(String nameEn){

        for(MapClassification mapCLS : MapClassification.values()){
            if(mapCLS.nameEn.equals(nameEn)){
                return mapCLS;
            }
        }
        return null;

    }

}
