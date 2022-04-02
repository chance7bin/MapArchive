package com.opengms.maparchivebackendprj.entity.po;

import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @Description
 * @Author bin
 * @Date 2021/10/19
 */
@Data
@Document
public class FileInfo extends GenericId {
    String fileName;  //文件名
    String path;  //文件绝对路径
    String md5;
    String type; //文件类型
    Date createTime = new Date();
}
