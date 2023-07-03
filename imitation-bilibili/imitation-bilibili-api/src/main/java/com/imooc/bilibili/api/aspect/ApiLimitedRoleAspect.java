package com.imooc.bilibili.api.aspect;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.auth.UserRole;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect//切面
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    //切点  在UserMomentsApi中的addUserMoments接口里使用了
    @Pointcut("@annotation(com.imooc.bilibili.domain.annotation.ApiLimitedRole)")//告诉springboot应该什么时候进行切入  在执行ApiLimitedRole注解的时候进行切入
    public void check(){
    }

    //在发生之前进行操作
    @Before("check() && @annotation(apiLimitedRole)")//@annotation(apiLimitedRole)以此来获取需要被限制的角色，再到ApiLimitedRole中的list中进行比对
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){//ApiLimitedRole apiLimitedRole要和@annotation(apiLimitedRole)中的变量名称一致
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();//获取到自定义apiLimitedRole注解中，事先设置的被限制的角色的唯一编码
        //比对当前用户角色是否在，限制角色列表里。如果在的话就不予访问该接口。
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());

        roleCodeSet.retainAll(limitedRoleCodeSet);//将两个set取交集  来代替遍历比对等复杂操作-----最后的结果会传给roleCodeSet，即为它们的交集
        if(roleCodeSet.size() > 0){
            throw new ConditionException("权限不足！");
        }
    }
}
