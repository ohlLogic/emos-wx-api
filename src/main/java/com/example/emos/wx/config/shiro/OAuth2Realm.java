package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 认证和授权方法
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 授权（验证权限时调用）
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        TbUser user = (TbUser) collection.getPrimaryPrincipal();
        int userId = user.getId();
        // 查询用户的权限列表
        Set<String> permsSet = userService.searchUserPermissions(userId);


        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 把权限列表添加到info对象中
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证（验证登录时调用）
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();
        //从令牌中获取userId，然后检测该账户是否被冻结
        int userId = jwtUtil.getUserId(accessToken);
        TbUser user = userService.searchById(userId);

        if(user == null){
            throw new LockedAccountException("账号已被锁定，请联系管理员");
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        return info;
    }
}
