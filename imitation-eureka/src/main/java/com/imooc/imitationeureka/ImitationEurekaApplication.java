package com.imooc.imitationeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEurekaServer
public class ImitationEurekaApplication {

    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ImitationEurekaApplication.class, args);

    }

}
