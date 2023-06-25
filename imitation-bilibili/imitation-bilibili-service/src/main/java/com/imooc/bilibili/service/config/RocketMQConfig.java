package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.service.UserFollowingService;
//import com.imooc.bilibili.service.websocket.WebSocketService;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//RocketMQ主要结构：名称服务器，代理服务器

@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.address}")//代表要在配置文件yml或者properties文件中读取这个变量值----rocketMQ名称服务器地址（一般自己配置）
    private String nameServerAddr;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;//RedisTemplate是spring-boot-starter-data-redis给我们提供的一个依赖包

    @Autowired
    private UserFollowingService userFollowingService;

    @Bean("momentsProducer")//用户动态生产者
    public DefaultMQProducer momentsProducer() throws Exception{
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddr);//名称服务器的地址---即生产者地址
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")//用户动态消费者
    public DefaultMQPushConsumer momentsConsumer() throws Exception{
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");//subExpression指下一级主题，*指跟这个主题相关的所有内容都要进行订阅
        consumer.registerMessageListener(new MessageListenerConcurrently() {//给消费者添加实时监听器   并发监听
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context){//rocketMQ中封装消息的扩种---MessageExt：属于rocketMQ的封装类
                MessageExt msg = msgs.get(0);//每次添加数据的时候，只默认向MQ中添加1条元素，所以msgs中只会有一条数据   根据场景适用性来具体设置，此场景下，每次一般一个用户只会增加一条动态
                if(msg == null){
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                String bodyStr = new String(msg.getBody());//byte数组转换为了相关的字符串
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);//把相关的实体类取出来----将对应的字符串转成相关的实体类
                Long userId = userMoment.getUserId();
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for(UserFollowing fan : fanList){
                    String key = "subscribed-" + fan.getUserId();//使用redis做缓存   此处为redis的key
                    String subscribedListStr = redisTemplate.opsForValue().get(key);//对redis中的数据进行操作
                    List<UserMoment> subscribedList;
                    if(StringUtil.isNullOrEmpty(subscribedListStr)){
                        subscribedList = new ArrayList<>();
                    }else{
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    subscribedList.add(userMoment);//将该subscribed-userId 键 Key所对应的所有消息封装到一个List中保存，List为值，用户直接在redis中查询就可以得到推送给自己的消息
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));//JSONObject.toJSONString(subscribedList)将subscribedList转换成Json字符串格式的数据

                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();//consumer启动
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception{
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动Producer实例
        producer.start();
        return producer;
    }

//    @Bean("danmusConsumer")
//    public DefaultMQPushConsumer danmusConsumer() throws Exception{
//        // 实例化消费者
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
//        // 设置NameServer的地址
//        consumer.setNamesrvAddr(nameServerAddr);
//        // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
//        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS, "*");
//        // 注册回调实现类来处理从broker拉取回来的消息
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//                MessageExt msg = msgs.get(0);
//                byte[] msgByte = msg.getBody();
//                String bodyStr = new String(msgByte);
//                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
//                String sessionId = jsonObject.getString("sessionId");
//                String message = jsonObject.getString("message");
//                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get(sessionId);
//                if(webSocketService.getSession().isOpen()){
//                    try {
//                        webSocketService.sendMessage(message);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                // 标记该消息已经被成功消费
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//        // 启动消费者实例
//        consumer.start();
//        return consumer;
//    }
}
