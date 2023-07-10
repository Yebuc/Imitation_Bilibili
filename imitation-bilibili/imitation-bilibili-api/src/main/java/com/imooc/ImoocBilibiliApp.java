package com.imooc;

import com.imooc.bilibili.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * @author Amber
 * @create 2023-06-18 14:14
 */
@SpringBootApplication
@EnableTransactionManagement//允许使用异步
@EnableAsync//允许使用异步
@EnableScheduling//允许使用定时任务
//@EnableAspectJAutoProxy//开启AOP
@CrossOrigin//允许跨域
@Slf4j
@EnableFeignClients(basePackages = "com.imooc.bilibili.service.feign")//扫描指定的自己定义的feign的包路径
@EnableHystrix//标注可以使用Hystrix，开启Hystrix功能---断路器功能
public class ImoocBilibiliApp {
    public static void main(String[] args){
        ApplicationContext app = SpringApplication.run(ImoocBilibiliApp.class,args);//启动方法
        WebSocketService.setApplicationContext(app);//WebSocketService中将ApplicationContext赋值给其中，让其可以操作上下文

//        Object corsConfig = app.getBean("CorsConfig");
//        System.out.println(corsConfig);
    }
}
