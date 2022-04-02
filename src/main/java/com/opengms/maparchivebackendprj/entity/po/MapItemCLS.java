package com.opengms.maparchivebackendprj.entity.po;

import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Data
@Document
public class MapItemCLS extends GenericId {
    List<String> childrenId = new ArrayList<>();
    String nameCn;
    String nameEn; //目前考虑的是只有与数据库表对应的目录（MapClassification中的）才有英文名，主要用处就是与数据库的表明对应
    String parentId;
}
