package com.opengms.maparchivebackendprj.service.impl;

import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.service.IGenericService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
public class GenericServiceImpl implements IGenericService {


    @Override
    public Pageable getPageable(FindDTO findDTO) {
        return PageRequest.of(findDTO.getPage()-1, findDTO.getPageSize(), Sort.by(findDTO.getAsc()? Sort.Direction.ASC: Sort.Direction.DESC,findDTO.getSortField()));
    }

    @Override
    public boolean checkFindDTOParams(FindDTO findDTO) {

        return isExist(findDTO.getAsc()) &&
            isExist(findDTO.getPage()) &&
            isExist(findDTO.getPageSize()) &&
            isExist(findDTO.getSortField());
    }

    @Override
    public boolean isEmptyString(String s) {
        return (s == null || s.equals(""));
    }

    @Override
    public boolean isNullParam(Object o) {
        return o == null ;
    }

    @Override
    public boolean isExist(Object o) {
        return o != null;
    }


}
