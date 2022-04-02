package com.opengms.maparchivebackendprj.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code=1;
    private String msg="success";
    private T data;
}
