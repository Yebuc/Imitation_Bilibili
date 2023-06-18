package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author Amber
 * @create 2023-06-18 14:14
 */
@SpringBootApplication
public class ImoocBilibiliApp {
    public static void main(String[] args){
        ApplicationContext app = SpringApplication.run(ImoocBilibiliApp.class,args);//启动方法
    }
}
