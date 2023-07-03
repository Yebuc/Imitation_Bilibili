package com.imooc.bilibili.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {//webSocket协议的配置类

    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();//服务器端点的导出者--->发现服务器上的端点并导出   用来发现websocket服务,通过ServerEndpoint来标识
    }
}
