package com.imooc.bilibili.api;

import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Amber
 * @create 2023-06-20 17:04
 */
@RestController
public class UserApi {

    @Autowired//这里使用 @Autowired有一些问题----还未解答
    private UserService userService;
    @Autowired
    private UserSupport userSupport;


    @GetMapping("/users")//得到当前用户的信息
    public JsonResponse<User> getUserInfo(){
        Long currentUserId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(currentUserId);
        return new JsonResponse<>(user);
    }

    @GetMapping("/rsa-pks")//得到RAS的公钥，提供给前端  因为在传输的过程中有可能请求被拦截，有泄露密码的风险，所以传输的过程数据不能是明文传输
    public JsonResponse<String> getRsaPublicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    @PostMapping("/users")//注册
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
    }
    @PostMapping("/user-tokens")//登录
    public JsonResponse<String> login(@RequestBody User user){
        String token = userService.login(user);
        return new JsonResponse<>(token);//返回token给前端
    }

    @PutMapping("/users")//更新User数据
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception{
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }
    @PutMapping("/user-infos")//更新userInfo数据
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long currentUserId = userSupport.getCurrentUserId();//userId一般都是从token中获取的，不能直接从前端传
        userInfo.setUserId(currentUserId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }


}
