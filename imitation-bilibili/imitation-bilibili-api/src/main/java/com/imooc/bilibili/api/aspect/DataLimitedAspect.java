package com.imooc.bilibili.api.aspect;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.DataLimited)")
    public void check(){
    }

    @Before("check()")
    public void doBefore(JoinPoint joinPoint){//ApiLimitedRoleAspect中有详细的注释
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);  //这里设置的逻辑是  Lv0只能传type为0的数据，剩下的角色可以传所有   可能有多个角色
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        Object[] args = joinPoint.getArgs();//得到当前所切方法的参数
        for(Object arg : args){
          if(arg instanceof UserMoment){
              UserMoment userMoment = (UserMoment)arg;
              String type = userMoment.getType();
              if(roleCodeSet.contains(AuthRoleConstant.ROLE_LV0) && !"0".equals(type)){
                  throw new ConditionException("参数异常");
              }
          }
        }
    }
}
