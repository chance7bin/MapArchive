package com.opengms.maparchivebackendprj.entity.enums;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
public enum ResultEnum {
    SUCCESS(1, "Success"),
    NO_OBJECT(0, "No object"),
    ERROR(-1,"Error"),
    UNAUTHORIZED(-2,"Unauthorized"),
    REMOTE_SERVICE_ERROR(-8, "远程服务调用出错");


    private Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
