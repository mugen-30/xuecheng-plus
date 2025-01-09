package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.IXcMenuService;
import com.xuecheng.ucenter.service.IXcUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    IXcUserService ixcUserService;

    @Resource
    IXcMenuService ixcMenuService;

    @Resource
    ApplicationContext applicationContext;

    /**
     * @param s 账号
     * @return org.springframework.security.core.userdetails.UserDetails
     * @description 根据账号查询用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        //获取认证类型
        String authType = authParamsDto.getAuthType();
        //根据认证类型获取对应的认证服务实现类
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用具体的认证服务实现类进行认证处理
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserPrincipal(xcUserExt);
    }

    /**
     * @param user 用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @description 查询用户信息
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        String password = user.getPassword();
        //用户权限
        String[] authorities = {"p1"};
        //根据用户id查询用户权限
        List<XcMenu> xcMenus = ixcMenuService.selectPermissionByUserId(user.getId());
        if (!xcMenus.isEmpty()){
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m->{
                //拿到用户拥有权限符
                permissions.add(m.getCode());
            });
            authorities = permissions.toArray(new String[0]);
        }
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }

}