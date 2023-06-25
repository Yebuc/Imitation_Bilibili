package com.imooc.bilibili.service.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMQUtil {//RocketMQ的工具包

    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{//同步发送信息
        SendResult result = producer.send(msg);
        System.out.println(result);
    }

    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{//异步发送信息
        producer.send(msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {//发送成功的回调
                Logger logger = LoggerFactory.getLogger(RocketMQUtil.class);
                logger.info("异步发送消息成功，消息id：" + sendResult.getMsgId());
            }
            @Override
            public void onException(Throwable e) {//发送失败的提醒
                e.printStackTrace();
            }
        });
    }
}
