package com.opengms.maparchivebackendprj.controller;

import cn.hutool.json.JSONObject;
import com.opengms.maparchivebackendprj.annotation.SuperLoginToken;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserLoginDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserRegisterDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserUpdatePwdDTO;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.User;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.IUserService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Api(tags = "用户登录注册")
@RestController
@RequestMapping(value = "/user")
@Slf4j
public class UserController {

    @Autowired
    IUserService userService;

    @Autowired
    IGenericService genericService;


    @ApiOperation(value = "用户注册功能" )
    @RequestMapping (value = "/register", method = RequestMethod.POST)
    public JsonResult doRegister(@RequestBody UserRegisterDTO user) {
        return userService.register(user);
    }


    @ApiOperation(value = "用户登录功能" )
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JsonResult doLogin(@RequestBody UserLoginDTO user){
        return userService.login(user.getName(),user.getPassword());
    }

    @ApiOperation(value = "修改密码" )
    @RequestMapping(value = "/update/pwd",method = RequestMethod.POST)
    public JsonResult updatePwd(@RequestBody UserUpdatePwdDTO user){

        return userService.updatePwd(user);
    }


    @SuperLoginToken
    @ApiOperation(value = "获取所有用户列表", notes = "@SuperLoginToken")
    @RequestMapping(value = "/all",method = RequestMethod.POST)
    public JsonResult getAllUser(@RequestBody FindDTO findDTO){
        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        return userService.findAllUser(findDTO);
    }

    @SuperLoginToken
    @ApiOperation(value = "获取user用户列表", notes = "@SuperLoginToken")
    @RequestMapping(value = "/role/user",method = RequestMethod.POST)
    public JsonResult getUserRoleUser(@RequestBody FindDTO findDTO){
        if (!genericService.checkFindDTOParams(findDTO)){
            return ResultUtils.error("参数错误");
        }

        return userService.getUserRoleUser(findDTO);
    }

    @SuperLoginToken
    @ApiOperation(value = "超级管理员修改用户权限", notes = "@SuperLoginToken")
    @RequestMapping(value="/update/{userId}/{role}",method = RequestMethod.GET)
    public JsonResult updateRole(@PathVariable String userId, @PathVariable UserRoleEnum role){

        return userService.updateRole(userId,role);
    }


    @SuperLoginToken
    @ApiOperation(value = "超级管理员删除用户", notes = "@SuperLoginToken")
    @RequestMapping(value = "/delete/{userId}",method = RequestMethod.POST)
    public JsonResult deleteUser(@PathVariable String userId) {


        return userService.deleteUser(userId);
    }



}
