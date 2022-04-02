package com.opengms.maparchivebackendprj.entity.dto.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Data
public class UserRegisterDTO {

    //用户个人信息，由用户直接填写
    String name; //用户昵称
    String password; //md5+sha256加密

    //联系方式
    String phone;

    //用户头像
    // String avatar = "";

    //用户权限管理
    // UserRoleEnum userRole = UserRoleEnum.USER;

    //用户相关资源数量统计
    // UserResourceCount resourceCount;

}
