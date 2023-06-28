package com.imooc.bilibili.api;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.api.support.UserSupport;
import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.PageResult;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.service.UserFollowingService;
import com.imooc.bilibili.service.UserService;
import com.imooc.bilibili.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private UserFollowingService userFollowingService;


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

    //用户分页查询的接口   是给关注用户来服务的
    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no, @RequestParam Integer size, String nick){
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();//JSONObject是在fastJson里面封装的包，里面实现了Map类以及内置了一些比较好用的方法，可以直接把它当作map来使用
        params.put("no", no);
        params.put("size", size);
        params.put("nick", nick);
        params.put("userId", userId);
        PageResult<UserInfo> result = userService.pageListUserInfos(params);
        if(result.getTotal() > 0){
            List<UserInfo> checkedUserInfoList = userFollowingService.checkFollowingStatus(result.getList(), userId);//检查关注的状态,---检查查出来的用户有没有被当前登录的用户关注过
            result.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(result);
    }

    //下面的三个接口主要功能是实现双token的登录模式---->access token 与 refresh token    yep


    @PostMapping("/user-dts")//dts--->double tokens  通过双token来登录   上方的单token方式返回的是string，而双token的返回数据是有所不同的！！！  返回的是双token
    public JsonResponse<Map<String, Object>> loginForDts(@RequestBody User user) throws Exception {
        Map<String, Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    @DeleteMapping("/refresh-tokens")//退出登录接口，需要将对应的refresh token给删除掉
    public JsonResponse<String> logout(HttpServletRequest request){
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken, userId);
        return JsonResponse.success();
    }

    @PostMapping("/access-tokens")//刷新access token给前端，与之前的access token是不一样的哦
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }




}
