package com.imooc.bilibili.service;

import com.imooc.bilibili.domain.auth.*;
import com.imooc.bilibili.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    public UserAuthorities getUserAuthorities(Long userId) {//获取用户权限
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);//通过userId来获取用户关联的角色   得到角色ID
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole :: getRoleId).collect(Collectors.toSet());//通过Lambda表达式获取到了角色id列表----注意一个用户可能是会有多个角色的
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);//获取操作权限
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {//新增默认角色，与初始化注册用户绑定
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
