package com.opengms.maparchivebackendprj.entity.enums;

import lombok.AllArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/16
 */
@AllArgsConstructor
public enum OperateTypeEnum {

    REGISTER("REGISTER"),
    LOGIN("LOGIN"),
    UPLOAD( "UPLOAD"),
    MOUNT( "MOUNT"),
    DOWNLOAD( "DOWNLOAD"),
    DELETE( "DELETE"),
    UPDATE( "UPDATE"),
    THUMBNAIL( "THUMBNAIL"),
    TILES( "TILES"),
    PROCESS( "PROCESS"),

    ;


    private String text;

    public String getText() {
        return text;
    }

}
