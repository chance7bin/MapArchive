package com.opengms.maparchivebackendprj.entity.dto.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Data
public class UserUpdatePwdDTO extends UserLoginDTO{

    @ApiModelProperty(value = "新密码", example = "admin1")
    String newPassword;

}
