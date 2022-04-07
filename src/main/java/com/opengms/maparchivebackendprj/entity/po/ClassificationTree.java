package com.opengms.maparchivebackendprj.entity.po;

import com.alibaba.fastjson.JSONArray;
import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/04/06
 */
@Data
public class ClassificationTree extends GenericId {

    String version; //版本
    JSONArray tree; //一整个分类树


}
