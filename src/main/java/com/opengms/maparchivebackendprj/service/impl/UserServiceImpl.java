package com.opengms.maparchivebackendprj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mongodb.client.result.DeleteResult;
import com.opengms.maparchivebackendprj.dao.ILogDao;
import com.opengms.maparchivebackendprj.dao.IUserDao;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.entity.bo.PageableResult;
import com.opengms.maparchivebackendprj.entity.dto.FindDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserRegisterDTO;
import com.opengms.maparchivebackendprj.entity.dto.User.UserUpdatePwdDTO;
import com.opengms.maparchivebackendprj.entity.enums.OperateTypeEnum;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.LogInfo;
import com.opengms.maparchivebackendprj.entity.po.User;
import com.opengms.maparchivebackendprj.service.IGenericService;
import com.opengms.maparchivebackendprj.service.ILogService;
import com.opengms.maparchivebackendprj.service.IUserService;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Description
 * @Author bin
 * @Date 2022/03/24
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    IUserDao userDao;


    @Autowired
    ILogDao logDao;


    @Autowired
    IGenericService genericService;

    @Override
    public String getToken(User user) {
        String token="";
        token= JWT.create().withAudience(user.getId())
            .sign(Algorithm.HMAC256(user.getPassword()));
        return token;
    }

    @Override
    public JsonResult register(UserRegisterDTO user) {
        //判断ID是否被注册过，如果则不会继续注册
        User existUser = userDao.findByName(user.getName());
        if (existUser != null){
            return ResultUtils.error("用户已存在");
        }
        user.setPassword(DigestUtils.sha256Hex(user.getPassword().getBytes()));
        //设置用户注册初始权限为未定义
        // user.setUserRole(UserRoleEnum.USER);

        User newUser = new User();
        BeanUtils.copyProperties(user,newUser);

        newUser = userDao.insert(newUser);

        LogInfo logInfo = new LogInfo(newUser.getName(), null, OperateTypeEnum.REGISTER, new Date());
        logDao.insert(logInfo);

        return ResultUtils.success(newUser);

    }

    @Override
    public JsonResult login(String name, String password) {
        User LoginUser=userDao.findByName(name);
        if(LoginUser == null){
            return ResultUtils.error("登录失败，用户不存在");
        }else{

            if(!LoginUser.getPassword().equals(DigestUtils.sha256Hex(password))){
                return ResultUtils.error("登录失败，密码错误");
            }else{
                JSONObject jsonObject = new JSONObject();
                String token = getToken(LoginUser);
                jsonObject.put("token",token);
                jsonObject.put("name",LoginUser.getName());
                jsonObject.put("userRole",LoginUser.getUserRole());

                // 在session放email做为操作的认证
                // setUserSession(request,LoginUser.getEmail());

                logDao.insert(new LogInfo(LoginUser.getName(),null,OperateTypeEnum.LOGIN,new Date()));

                return ResultUtils.success(jsonObject);
            }
        }
    }

    @Override
    public User getUserByToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");// 从 http 请求头中取出 token
        String token=null;
        if(authorization!=null){
            // 因为前端传过来的token长这样: "token eyJ...ciOM89Y"
            // 真的token从第6位开始
            token = authorization.substring(6);
        }
        if (token == null) {
            return null;
        }
        String userId = JWT.decode(token).getAudience().get(0);
        if (userId == null) {
            return null;
        }
        User user = userDao.findById(userId);
        if (user == null) {
            return null;
        }else{
            return user;
        }
    }

    @Override
    public JsonResult findAllUser(FindDTO findDTO) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<User> userList = userDao.findAll(pageable);

        long count = userDao.countAll();

        return ResultUtils.success(new PageableResult<>(count,userList));
    }

    @Override
    public JsonResult getUserRoleUser(FindDTO findDTO) {

        Pageable pageable = genericService.getPageable(findDTO);

        List<User> userList = userDao.findAllByUserRole(UserRoleEnum.USER, pageable);

        long count = userDao.countAllByUserRole(UserRoleEnum.USER);

        return ResultUtils.success(new PageableResult<>(count,userList));
    }

    @Override
    public JsonResult updateRole(String userId, UserRoleEnum role) {

        User user = userDao.findById(userId);
        if(user == null){
            return ResultUtils.error("未找到该用户");
        }
        user.setUserRole(role);
        userDao.save(user);


        return ResultUtils.success();
    }

    @Override
    public JsonResult deleteUser(String userId) {
        User user = userDao.findById(userId);
        if(user == null){
            return ResultUtils.error("未找到该用户");
        }

        userDao.delete(user);

        return ResultUtils.success();
    }

    @Override
    public JsonResult updatePwd(UserUpdatePwdDTO user) {

        if (user.getPassword().equals(user.getNewPassword())){
            return ResultUtils.error("两次密码相同，请输入新的密码");
        }


        User old = userDao.findByName(user.getName());

        if(!old.getPassword().equals(DigestUtils.sha256Hex(user.getPassword()))){
            return ResultUtils.error("旧密码输入错误");
        }

        old.setPassword(DigestUtils.sha256Hex(user.getNewPassword().getBytes()));
        return ResultUtils.success(userDao.save(old));

    }


}
