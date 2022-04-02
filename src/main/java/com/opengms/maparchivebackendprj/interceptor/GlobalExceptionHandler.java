package com.opengms.maparchivebackendprj.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.opengms.maparchivebackendprj.entity.bo.JsonResult;
import com.opengms.maparchivebackendprj.utils.ResultUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author jinbin
 * @date 2018-07-08 22:37
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public JsonResult handleException(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.equals("")) {
            msg = "服务器出错";
        }
        // JSONObject jsonObject = new JSONObject();
        // jsonObject.put("message", msg);
        return ResultUtils.error(msg);
    }
}
