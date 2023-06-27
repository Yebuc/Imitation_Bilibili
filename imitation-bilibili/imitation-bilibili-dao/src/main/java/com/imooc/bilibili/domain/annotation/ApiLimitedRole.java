package com.imooc.bilibili.domain.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;
//api权限控制接口aop注解
@Retention(RetentionPolicy.RUNTIME)//注解作用在运行阶段
@Target({ElementType.METHOD})//注解目标是方法---作用在方法上注解
@Documented
@Component
public @interface ApiLimitedRole {//针对于接口的权限所设计的注解   做aop切面的时候需要使用到

    String[] limitedRoleCodeList() default {};//相关的--我们需要判断或限制的角色对象，的唯一编码，的列表     比如哪些角色系统会针对其api接口调用进行限制
}
