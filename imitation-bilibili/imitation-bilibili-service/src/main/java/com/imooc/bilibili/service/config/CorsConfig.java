package com.imooc.bilibili.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 跨域解决配置
 *
 * 跨域概念：
 *      出于浏览器的同源策略限制，同源策略会阻止一个域的javascript脚本和另外一个域的内容进行交互。
 *      所谓同源就是指两个页面具有相同的协议（protocol），主机（host）和端口号（port）
 *
 * 非同源的限制：
 *  【1】无法读取非同源网页的 Cookie、LocalStorage 和 IndexedDB
 *  【2】无法接触非同源网页的 DOM
 *  【3】无法向非同源地址发送 AJAX 请求
 *
 *  spingboot解决跨域方案：CORS 是跨域资源分享（Cross-Origin Resource Sharing）的缩写。
 *  它是 W3C 标准，属于跨源 AJAX 请求的根本解决方法。
 *
 *
 *  Filter是用来过滤任务的，既可以被使用在请求资源，也可以是资源响应，或者二者都有
 *  Filter使用doFilter方法进行过滤
 */

@Configuration
@WebFilter(filterName = "CorsConfig")
@Slf4j
@Order(-1)
public class CorsConfig implements Filter {
//public class CorsConfig{
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        log.error("我进来了嘛？");
//
//        // 所有和跨域有关的配置写在CorsConfiguration中
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//
//        // 1、配置跨域 头、方法、来源都不限制
//        corsConfiguration.addAllowedHeader("*");
//        corsConfiguration.addAllowedMethod("*");
//        corsConfiguration.addAllowedOrigin("*");
//        corsConfiguration.setAllowCredentials(true);  // 跨域请求允许携带cookie
//
//        // 设置跨域配置，任意访问路径都可
//        source.registerCorsConfiguration("/**", corsConfiguration);
//
//        log.error("我进来了呀！！！");
//
//        return new CorsWebFilter(source);
//    }
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                //是否发送Cookie
//                .allowCredentials(true)
//                //放行哪些原始域
//                .allowedOrigins("*")
//                .allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE"})
//                .allowedHeaders("*")
//                .exposedHeaders("*");
//    }


    private final String[] allowedDomain = {"http://localhost:15005", "http://39.107.54.180","http://localhost:8080"};


    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Set<String> allowedOrigins= new HashSet<>(Arrays.asList(allowedDomain));
        String origin=httpRequest.getHeader("Origin");
        if (origin == null) {
            chain.doFilter(request, response);
            return;
        }
        log.error("我进来了嘛？");
        if (allowedOrigins.contains(origin)){
            log.error("进来了哦！！");
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
            httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With, userId, token, ut");//表明服务器支持的所有头信息字段
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true"); //如果要把Cookie发到服务器，需要指定Access-Control-Allow-Credentials字段为true;
            httpResponse.setHeader("XDomainRequestAllowed","1");
        }
        chain.doFilter(request, response);
    }


//    public void init(FilterConfig filterConfig){}
//
//    public void destroy() {
//    }


//    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//        HttpServletResponse response = (HttpServletResponse) res;
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//        response.setHeader("Access-Control-Max-Age", "3600");
//        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
//        chain.doFilter(req, res);
//    }
//    public void init(FilterConfig filterConfig) {}
//    public void destroy() {}


}
