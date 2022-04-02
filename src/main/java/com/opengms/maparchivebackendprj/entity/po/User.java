package com.opengms.maparchivebackendprj.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opengms.maparchivebackendprj.entity.bo.GenericId;
import com.opengms.maparchivebackendprj.entity.bo.user.GeoInfoMeta;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author bin
 * @Date 2021/11/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class User extends GenericId {

    //用户个人信息，由用户直接填写
    String name; //用户昵称
    String password; //md5+sha256加密

    //联系方式
    String phone;

    //用户头像
    // String avatar = "";

    //用户权限管理
    UserRoleEnum userRole = UserRoleEnum.USER;

    //用户相关资源数量统计
    // UserResourceCount resourceCount;

    //用户权限管理
    // List<Boolean> permissions;//0 是修改 ；1是查看 ；2是删除

    //用户注册时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    Date createTime = new Date();

}
