package com.imooc.bilibili.service.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.Collections;

/**
 * @author Amber
 * @create 2023-06-20 15:36
 */
@Configuration
public class JsonHttpMessageConverterConfig {

    @Bean
    @Primary
    public HttpMessageConverters fastJsonHttpMessageConverter(){//Json转换数据类---将json与后端所传递的Message互相转换，构建一个转换规则
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,//格式化输出
                SerializerFeature.WriteNullStringAsEmpty,//允许输出为空  把value为null的字段转换为null输出,即可以显示为空
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.MapSortField,//默认升序排序，对map格式的数据进行排序
                SerializerFeature.DisableCircularReferenceDetect//禁用循环引用
        );//Json数据序列化
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //如果使用feign进行微服务间的接口调用，则需要加上该配置
//        fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverters(fastConverter);
    }
}
