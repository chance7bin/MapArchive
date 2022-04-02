package com.opengms.maparchivebackendprj.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * @Description 日志记录信息 处理文件/上传文件/下载文件
 * @Author bin
 * @Date 2022/03/16
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class LogInfo extends GenericId {
    String operator;  //操作人员姓名
    List<String> itemListId;  //操作条目id
    OperateTypeEnum operateType;  //操作类型
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    Date createTime; //操作时间
}
