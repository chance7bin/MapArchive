package com.opengms.maparchivebackendprj.entity.enums;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MapTypeEnum {
    // 基本比例尺中地图种类属性字段值
    地形图("地图种类","地形图"),
    航空图("地图种类","航空图"),
    联合作战图("地图种类","联合作战图"),
    协同图("地图种类","协同图"),
    ;


    private String field;
    private String name;


    public String getField(){ return field; }
    public String getName(){ return name; }


//    public static String getMapFieldByName( String name ){
//        for(MapTypeEnum item : MapTypeEnum.values()){
//            if(item.name.equals(name)){
//                return item.getField();
//            }
//        }
//        return null;
//    }



}
