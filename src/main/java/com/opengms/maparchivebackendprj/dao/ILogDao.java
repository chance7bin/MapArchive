package com.opengms.maparchivebackendprj.dao;

import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface ILogDao {

    LogInfo insert(LogInfo logInfo);


    List<LogInfo> findAll(Pageable pageable);

    long countAll();

    List<LogInfo> findAllByOperateType(OperateTypeEnum operateType, Pageable pageable);

    long countAllByOperateType(OperateTypeEnum operateType);
}
