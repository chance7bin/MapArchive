package com.opengms.maparchivebackendprj.entity.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opengms.maparchivebackendprj.entity.enums.MapClassification;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/08
 */
@Data
public class GenericItem extends GenericId {

    // String accessId; //访问条目路径id
    String name; //条目名
    String author; //条目创建者email
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    Date createTime = new Date(); //创建时间
    // int viewCount = 0; //访问数量

}
