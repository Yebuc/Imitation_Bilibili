package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserMomentsDao;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class UserMomentsService {

    @Autowired
    private UserMomentsDao userMomentsDao;

    @Autowired
    private ApplicationContext applicationContext;//ApplicationContext可以获取到和spring配置相关的所有上下文以及所有bean

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void addUserMoments(UserMoment userMoment) throws Exception {
        userMoment.setCreateTime(new Date());
        //todo 应该在做一个判断，是否有发布内容，即contentId是否为空，不能发布空动态

        userMomentsDao.addUserMoments(userMoment);//新增动态    往下应该往MQ之中发送一条消息，来告诉相关的订阅者，发布了一条新的动态
        //获取到新生成了producer
        DefaultMQProducer producer = (DefaultMQProducer)applicationContext.getBean("momentsProducer");//使用applicationContext.getBean获取到RocketMQConfig中的momentsProducer的bean
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
        RocketMQUtil.syncSendMsg(producer, msg);//同步发送消息方法   将同步动态消息发送到MQ中
    }

    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = "subscribed-" + userId;
        String listStr = redisTemplate.opsForValue().get(key);
        return JSONArray.parseArray(listStr, UserMoment.class);
    }
}
