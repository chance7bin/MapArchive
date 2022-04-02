package com.opengms.maparchivebackendprj.interceptor;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.opengms.maparchivebackendprj.annotation.PassToken;
import com.opengms.maparchivebackendprj.annotation.SuperLoginToken;
import com.opengms.maparchivebackendprj.annotation.UserLoginToken;
import com.opengms.maparchivebackendprj.dao.IUserDao;
import com.opengms.maparchivebackendprj.dao.impl.UserDaoImpl;
import com.opengms.maparchivebackendprj.entity.enums.UserRoleEnum;
import com.opengms.maparchivebackendprj.entity.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Optional;


/**
 * @author jinbin
 * @date 2018-07-08 20:41
 */
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    IUserDao userDao;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {

        String authorization = httpServletRequest.getHeader("Authorization");// 从 http 请求头中取出 token
        String token=null;
        if(authorization!=null){
            token = authorization.substring(6); //前端传过来的格式是"token xxxxxxxxxx "
        }
        // 如果不是映射到方法直接通过
        if(!(object instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod=(HandlerMethod)object;
        Method method=handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        //检查有没有超级用户权限的注解
        if(method.isAnnotationPresent(SuperLoginToken.class)){
            SuperLoginToken superLoginToken=method.getAnnotation(SuperLoginToken.class);
            if(superLoginToken.required()){
                //执行认证
                if(token==null){
                    throw new RuntimeException("无token，请重新登录");
                }
                //获取token中的user id
                String userId;
                try {
                    userId = JWT.decode(token).getAudience().get(0);
                } catch (JWTDecodeException j) {
                    throw new RuntimeException("用户验证失败，请重新登录");
                }
                User user= userDao.findById(userId);
                if ((user == null)) {
                    throw new RuntimeException("用户不存在，请重新登录");
                }else{
                    UserRoleEnum role = user.getUserRole();
                    if(role == UserRoleEnum.ROOT){
                        // 验证 token
                        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
                        try {
                            jwtVerifier.verify(token);
                        } catch (JWTVerificationException e) {
                            throw new RuntimeException("用户验证失败，请重新登录");
                        }
                        return true;
                    }else{
                        throw new RuntimeException("无super权限");
                    }

                }

            }
        }
        //检查有没有需要用户权限的注解
        if(method.isAnnotationPresent(UserLoginToken.class)){
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null) {
                    throw new RuntimeException("无token，请重新登录");
                }
                // 获取 token 中的 user id
                String userId;
                try {
                    userId = JWT.decode(token).getAudience().get(0);
                } catch (JWTDecodeException j) {
                    throw new RuntimeException("用户验证失败，请重新登录");
                }

                User user= userDao.findById(userId);
                if (user == null) {
                    throw new RuntimeException("用户不存在，请重新登录");
                }else{
                    // 验证 token
                    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
                    try {
                        jwtVerifier.verify(token);
                    } catch (JWTVerificationException e) {
                        throw new RuntimeException("用户验证失败，请重新登录");
                    }
                    return true;
                }

            }
        }

        // 需要用户登录走这里
        // 有 @LoginRequired 注解，需要认证
        // if(method.isAnnotationPresent(LoginRequired.class)){
        //     HttpSession session = httpServletRequest.getSession();
        //     Object userOid_obj = session.getAttribute("email");
        //     LoginRequired methodAnnotation = method.getAnnotation(LoginRequired.class);
        //     String[] arr = method.getReturnType().getName().split("\\.");
        //     String name = arr[arr.length - 1];
        //     if (methodAnnotation != null) {
        //         // 这写你拦截需要干的事儿，比如取缓存，SESSION，权限判断等
        //         if (userOid_obj==null){
        //             // 判断拦截的方法返回值是什么，如果是JsonResult返回JSON，如果是ModelAndView跳转到login页面
        //             if (name.equals("JsonResult")){
        //                 JsonResult unauthorized = ResultUtils.unauthorized();
        //                 JSONObject jsonObject = new JSONObject();
        //                 jsonObject.put("code",unauthorized.getCode());
        //                 jsonObject.put("msg",unauthorized.getMsg());
        //                 returnJson(httpServletResponse,jsonObject.toJSONString());
        //             }
        //             else {
        //                 //若未登录，则记录用户试图访问的接口，在登录后自动请求
        //                 // session.setAttribute("preUrl",request.getRequestURI());
        //                 // response.sendRedirect("/user/login");
        //             }
        //             return false;
        //         }
        //
        //         return true;
        //     }
        // }



        return true;
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception{
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
            log.error("response error",e);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
