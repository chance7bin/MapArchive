package com.opengms.maparchivebackendprj.entity.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableResult<T> {

    long count;
    List<T> content;

}
