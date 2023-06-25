package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.UserFollowingDao;
import com.imooc.bilibili.domain.FollowingGroup;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserFollowing;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserFollowingService {

    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    @Transactional//提供数据库事务服务，保证操作的原子性！！！---因为这个services里面有先删除后增加的更新操作，为了防止数据丢失便于回滚，使用事务操作
    public void addUserFollowings(UserFollowing userFollowing) {//增加用户的粉丝following   添加用户关注方法
        Long groupId = userFollowing.getGroupId();
        if(groupId == null){
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());//问题：？？type不是唯一性标识符啊，可能会有多个id，你是如何确定下来的?
        }else{
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if(followingGroup == null){
                throw new ConditionException("关注分组不存在！");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if(user == null){
            throw new ConditionException("关注的用户不存在！");
        }
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    // 第一步：获取关注的用户列表
    // 第二步：根据被关注用户的id查询被关注用户的基本信息
    // 第三步：将被关注用户按关注分组进行分类
    public List<FollowingGroup> getUserFollowings(Long userId){
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId);//获取用户id获取其关注的用户列表
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());//得到被所有关注用户的userId

        List<UserInfo> userInfoList = new ArrayList<>();
        if(followingIdSet.size() > 0){
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);//得到所有被关注用户的基本消息列表----userInfo表中的
        }
        for(UserFollowing userFollowing : list){//将UserFollowing消息补充完全，将userInfo也加入其中供前端展示  被关注用户消息的匹配
            for(UserInfo userInfo : userInfoList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }

        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId);//根据用户id将用户相关的所有用户分组给查出来
        FollowingGroup allGroup = new FollowingGroup();//获取全部的关注
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        List<FollowingGroup> result = new ArrayList<>();
        result.add(allGroup);
        for(FollowingGroup group : groupList){
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing : list){
                if(group.getId().equals(userFollowing.getGroupId())){
                    infoList.add(userFollowing.getUserInfo());//按分组给划分UserInfoList
                }

            }
            group.setFollowingUserInfoList(infoList);//按分组给划分UserInfoList
            result.add(group);
        }
        return result;
    }

    // 第一步：获取当前用户的粉丝列表
    // 第二步：根据粉丝的用户id查询基本信息
    // 第三步：查询当前用户是否已经关注该粉丝  互粉状态
    public List<UserFollowing> getUserFans(Long userId){//获取用户粉丝列表的方法
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);//这里的userId是表中的followingId
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());//将粉丝id抽取出来
        List<UserInfo> userInfoList = new ArrayList<>();
        if(fanIdSet.size() > 0){
            userInfoList = userService.getUserInfoByUserIds(fanIdSet);//获取到了粉丝列表中粉丝的userInfo
        }

        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId);//得到当前userId的关注列表，同粉丝列表fanList对比，判断有没有互相关注过的粉丝
        for(UserFollowing fan : fanList){
            for(UserInfo userInfo : userInfoList){//首先对粉丝列表中的userInfo赋值，得到相应的粉丝列表中粉丝的基本信息
                if(fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false);//标志位，标识当前用户是否关注了
                    fan.setUserInfo(userInfo);
                }
            }
            for(UserFollowing following : followingList){//获取互相关注中，粉丝列表中粉丝的数据
                if(following.getFollowingId().equals(fan.getUserId())){
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {//添加用户关注分组  返回的是followingGroup的主键id
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);//设置type的类别  UserConstant中有设置
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {//检查关注的状态,---检查查出来的用户有没有被当前登录的用户关注过
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for(UserInfo userInfo : userInfoList){
            userInfo.setFollowed(false);//先全部置为false
            for(UserFollowing userFollowing : userFollowingList){
                if(userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
