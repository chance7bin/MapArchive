package com.opengms.maparchivebackendprj.entity.enums;

import lombok.AllArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/16
 */
@AllArgsConstructor
public enum OperateTypeEnum {

    REGISTER(0,"REGISTER"),
    LOGIN(1,"LOGIN"),
    UPLOAD(2, "UPLOAD"),
    DOWNLOAD(3, "DOWNLOAD"),
    DELETE(4, "DELETE"),
    UPDATE(5, "UPDATE"),
    THUMBNAIL(6, "THUMBNAIL"),
    TILES(7, "TILES");

    private int number;
    private String text;

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

}
