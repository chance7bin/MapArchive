package com.opengms.maparchivebackendprj.entity.enums;

import lombok.AllArgsConstructor;

/**
 * @Description
 * @Author bin
 * @Date 2021/12/05
 */
@AllArgsConstructor
public enum StatusEnum {

    //Started: 1, Finished: 2, Inited: 0, Error: -1

    Inited(0, "Inited"),
    Started(1, "Started"),
    Finished(2, "Finished"),
    Error(-1, "Error");

    private int number;
    private String text;

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }
}
