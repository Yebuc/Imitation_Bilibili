package com.imooc.bilibili.api;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.UserMoment;
import com.imooc.bilibili.domain.annotation.ApiLimitedRole;
import com.imooc.bilibili.domain.annotation.DataLimited;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import com.imooc.bilibili.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserMomentsApi {

    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;


    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0})//advice标志注解---aop相关
    @DataLimited//advice标志注解---aop相关
    @PostMapping("/user-moments")//添加用户动态---也叫新增用户动态
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception {
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    @GetMapping("/user-subscribed-moments")//查询用户关注的动态消息
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }

}
