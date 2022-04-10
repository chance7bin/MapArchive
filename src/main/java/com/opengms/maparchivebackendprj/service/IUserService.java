package com.opengms.maparchivebackendprj.service;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserRegisterDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserUpdatePwdDTO;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
public interface IUserService {

    String getToken(User user);

    JsonResult register(UserRegisterDTO user);

    JsonResult login(String name, String password);

    User getUserByToken(HttpServletRequest request);

    JsonResult findAllUser(FindDTO findDTO);

    JsonResult getUserRoleUser(FindDTO findDTO);

    JsonResult updateRole(String userId, UserRoleEnum role);

    JsonResult deleteUser(String userId);

    JsonResult updatePwd(UserUpdatePwdDTO user);
}
