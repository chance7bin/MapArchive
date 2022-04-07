package com.opengms.maparchivebackendprj.entity.po;

import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import lombok.Data;

/**
 * @Description 存储的是所有元数据表格在数据库的集合名字
 * @Author bin
 * @Date 2022/04/06
 */
@Data
public class MetadataTable extends GenericId {
    String name; //元数据表格的中文名
    String collection; //对应数据库collection名字
}
