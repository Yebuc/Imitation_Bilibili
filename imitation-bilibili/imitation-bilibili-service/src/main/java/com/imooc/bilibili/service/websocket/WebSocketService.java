package com.imooc.bilibili.service.websocket;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.Danmu;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.DanmuService;
import com.imooc.bilibili.service.util.RocketMQUtil;
import com.imooc.bilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/imserver/{token}")//一旦一个类被@ServerEndpoint给标注了，那么它就是一个和websocket相关的服务类了   访问的时候依照里面的路径来访问就可以了
public class WebSocketService {

    private final Logger logger =  LoggerFactory.getLogger(this.getClass());//把当前logger所在的class，放在logger当中，既可以获取到和这个类相关的日志内容 日志记录

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);//当前长连接的人数
    //AtomicInteger--->java给提供的一个原子性操作的一个类   线程安全

    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    private Session session;//服务端和客户端进行通信的会话--->此Session主要是依赖进行长连接通信的

    private String sessionId;

    private Long userId;

    private static ApplicationContext APPLICATION_CONTEXT;//通过ApplicationContext来获取相关的实体类与bean，从某些角度上解决了springBoot单例注入的弊端

    public static void setApplicationContext(ApplicationContext applicationContext){//在启动类ImoocBilibiliApp里面调用与赋值
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    @OnOpen//用来标识，当我们连接成功之后就要来调用这个注解标识的相关方法--->websocket相关的注解
    public void openConnection(Session session, @PathParam("token") String token){//打开连接
        try{
            this.userId = TokenUtil.verifyToken(token);
        }catch (Exception ignored){}
        this.sessionId = session.getId();
        this.session = session;
        if(WEBSOCKET_MAP.containsKey(sessionId)){//更新Session
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        }else{
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();//在线人数+1
        }
        logger.info("用户连接成功：" + sessionId + "，当前在线人数为：" + ONLINE_COUNT.get());//因为ONLINE_COUNT为final
        try{
            this.sendMessage("0");//告诉前端通过websocket连接成功
        }catch (Exception e){
            logger.error("连接异常");
        }
    }

    @OnClose
    public void closeConnection(){//关闭连接  关了视频就直接没有了
        if(WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.info("用户退出：" + sessionId + "当前在线人数为：" + ONLINE_COUNT.get());
    }

    @OnMessage
    public void onMessage(String message){//当有消息进行通信的时候   这里的message不只是弹幕内容，还有一些弹幕相关的属性，由前端传送
        logger.info("用户信息：" + sessionId + "，报文：" + message);
        if(!StringUtil.isNullOrEmpty(message)){
            try{
                //群发消息          要使用并发+队列
                for(Map.Entry<String, WebSocketService> entry : WEBSOCKET_MAP.entrySet()){//每一个客户端连接都有它的webSocketService----->多例模式
                    WebSocketService webSocketService = entry.getValue();
                    //使用RocketMQ队列
                    DefaultMQProducer danmusProducer = (DefaultMQProducer)APPLICATION_CONTEXT.getBean("danmusProducer");
                    JSONObject jsonObject = new JSONObject();//其实就可以当作一个map来用,,一个加强版的map
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocketService.getSessionId());            //将jsonObject进行格式转化
                    Message msg = new Message(UserMomentsConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);//异步执行---RocketMQUtil里面封装好了
//                    webSocketService.sendMessage(message);//群发消息  这一步放到RocketMQConfig中的120行---->由监听器处理，发送给前端客户端,完成消息的推送
                }
                if(this.userId != null){
                    //保存弹幕到数据库   需要使用异步去做
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    //todo 还可以优化--->引入一个MQ消峰，再异步保存到数据库中进行持久化
                    DanmuService danmuService = (DanmuService)APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.asyncAddDanmu(danmu);//异步保存弹幕至数据库,不会占用主线程过多的时间
                    //保存弹幕到redis
                    danmuService.addDanmusToRedis(danmu);
                }
            }catch (Exception e){
                logger.error("弹幕接收出现问题");
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Throwable error){//当发生错误的时候需要处理
    }

    public void sendMessage(String message) throws IOException {//将消息发送到前端 --->发送消息
        this.session.getBasicRemote().sendText(message);//发送文本
    }

    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=5000)
    private void noticeOnlineCount() throws IOException {
        for(Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()){
            WebSocketService webSocketService = entry.getValue();
            if(webSocketService.session.isOpen()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());//给前端的提示语--->可有可无其实
                webSocketService.sendMessage(jsonObject.toJSONString());//转成json格式字符串
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
}
