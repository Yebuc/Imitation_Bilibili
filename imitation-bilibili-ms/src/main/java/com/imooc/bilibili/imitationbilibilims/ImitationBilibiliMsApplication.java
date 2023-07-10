package com.imooc.bilibili.imitationbilibilims;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient

public class ImitationBilibiliMsApplication {

    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ImitationBilibiliMsApplication.class, args);
    }

}
