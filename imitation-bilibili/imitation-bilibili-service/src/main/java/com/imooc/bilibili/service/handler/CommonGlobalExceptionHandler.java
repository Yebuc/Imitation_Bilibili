package com.imooc.bilibili.service.handler;


import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {//全局的异常处理器

    @ExceptionHandler(value = Exception.class)//处理的异常类型，这里Exception.class指只要抛出了异常，都要使用这个异常处理器来进行处理
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e){
        String errorMsg = e.getMessage();
        if(e instanceof ConditionException){
            String errorCode = ((ConditionException)e).getCode();
            return new JsonResponse<>(errorCode, errorMsg);//定制化处理错误信息
        }else{
            return new JsonResponse<>("500",errorMsg);//500一般为服务器端出现错误 即后端错误
        }
    }
}
