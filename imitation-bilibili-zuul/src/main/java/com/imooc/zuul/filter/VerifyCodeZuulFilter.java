package com.imooc.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class VerifyCodeZuulFilter extends ZuulFilter {//对于验证码进行验证的过滤器   当我们继承了ZuulFilter之后，就拥有了zuul网关过滤器的功能

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    //前端会传过来一对serialNumber=1&verifyCode=1序列号与验证码，后端会用redis键值对将其保存在redis当中
    //验证码传输过来之后，后端会对其进行比对，看是否和redis中保存的验证码一致，如果不一致则不予理会该请求,并返回报错信息给前端进行下一步操作；如果一致就认定这是一个合理的请求，会给其通过

    //是否过滤
    @Override
    public boolean shouldFilter() {
        //请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        //获取HttpServletRequest对象
        HttpServletRequest request = requestContext.getRequest();
        //取出表单序列号
        String serialNumber = request.getParameter("serialNumber");
        //如果存在验证码，则启用过滤器进行过滤
        return !StringUtil.isNullOrEmpty(serialNumber);
    }

    //过滤器逻辑方法
    @Override
    public Object run() throws ZuulException {
        //请求上下文
        RequestContext requestContext = RequestContext.getCurrentContext();
        //获取HttpServletRequest对象
        HttpServletRequest request = requestContext.getRequest();
        String serialNumber = request.getParameter("serialNumber");
        String verifyCode = request.getParameter("verifyCode");
        //从redis中取出验证码
        String redisVerifyCode = redisTemplate.opsForValue().get(serialNumber);
        if(!verifyCode.equals(redisVerifyCode)){
            //不在转发请求
            requestContext.setSendZuulResponse(false);
            //设置HTTP响应码为401（未授权）
            requestContext.setResponseStatusCode(401);

            //构建一个返回体，设置一下返回参数的格式--->未授权请求的返回格式
            requestContext.getResponse().
                    setContentType("application/json");
            String msg = "{\n" +
                    "    \"success\":false,\n" +
                    "    \"msg\": \"Code is incorrect\"\n" +
                    "}";
            requestContext.setResponseBody(msg);
        }

        //验证通过
        return null;
    }

    //过滤器类型，一共四种：pre请求前；route处理请求；post请求后；error请求错误时
    @Override
    public String filterType() {
        return "pre";
    }

    //过滤器顺序，值越小优先级越高
    @Override
    public int filterOrder() {
        return 0;
    }


}
