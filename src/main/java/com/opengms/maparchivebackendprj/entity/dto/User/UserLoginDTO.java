package com.opengms.maparchivebackendprj.entity.dto.User;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Data
public class UserLoginDTO {

    @ApiModelProperty(value = "用户名", example = "admin")
    String name;
    @ApiModelProperty(value = "密码", example = "admin")
    String password;

}
