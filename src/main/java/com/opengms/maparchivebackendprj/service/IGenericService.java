package com.opengms.maparchivebackendprj.service;

import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import org.springframework.data.domain.Pageable;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IGenericService {

    Pageable getPageable(FindDTO findDTO);

    //验证FindDTO的参数是否正确
    boolean checkFindDTOParams(FindDTO findDTO);

    boolean isEmptyString(String s);

    boolean isNullParam(Object o);

    boolean isExist(Object o);

}
