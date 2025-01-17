package com.xuecheng.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.model.dto.AuthParamsDto;
import com.xuecheng.auth.model.dto.XcUserExt;
import com.xuecheng.auth.model.po.XcUser;
import com.xuecheng.auth.model.po.XcUserRole;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.auth.service.IXcUserRoleService;
import com.xuecheng.auth.service.IXcUserService;
import com.xuecheng.auth.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("wx_authservice")
@EnableAspectJAutoProxy(exposeProxy = true)
public class WxAuthServiceImpl implements AuthService, WxAuthService {
    @Resource
    IXcUserService iXcUserService;
    @Resource
    IXcUserRoleService iXcRoleUserService;
    @Resource
    RestTemplate restTemplate;



    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;

    public XcUser wxAuth(String code) {

        //收到code调用微信接口申请access_token
        Map<String, String> access_token_map = getAccess_token(code);
        if (access_token_map == null) {
            return null;
        }
        System.out.println(access_token_map);
        String openid = access_token_map.get("openid");
        String access_token = access_token_map.get("access_token");
        //拿access_token查询用户信息
        Map<String, String> userinfo = getUserinfo(access_token, openid);
        if (userinfo == null) {
            return null;
        }
        //添加用户到数据库
        WxAuthServiceImpl wxAuthService = (WxAuthServiceImpl) AopContext.currentProxy();
        XcUser xcUser = wxAuthService.addWxUser(userinfo);

        return xcUser;
    }

    /**
     * 申请访问令牌,响应示例
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getAccess_token(String code) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, appid, secret, code);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }

    /**
     * 获取用户信息，示例如下：
     * {
     * "openid":"OPENID",
     * "nickname":"NICKNAME",
     * "sex":1,
     * "province":"PROVINCE",
     * "city":"CITY",
     * "country":"COUNTRY",
     * "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     * "privilege":[
     * "PRIVILEGE1",
     * "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getUserinfo(String access_token, String openid) {

        String wxUrl_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template, access_token, openid);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        //防止乱码进行转码
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);

        return resultMap;
    }

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 账号
        String username = authParamsDto.getUsername();
        XcUser user = iXcUserService.getOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }


    @Transactional
    public XcUser addWxUser(Map<String, String> user_info_map){
        // 1. 获取用户唯一标识：unionid作为用户的唯一表示
        String unionid = user_info_map.get("unionid");
        // 2. 根据唯一标识，判断数据库是否存在该用户
        XcUser xcUser = iXcUserService.getOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        // 2.1 存在，则直接返回
        if (xcUser != null){
            return xcUser;
        }
        // 2.2 不存在，新增
        xcUser = new XcUser();
        // 2.3 设置主键
        String uuid = UUID.randomUUID().toString();
        xcUser.setId(uuid);
        // 2.4 设置其他数据库非空约束的属性
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(user_info_map.get("nickname"));
        xcUser.setUserpic(user_info_map.get("headimgurl"));
        xcUser.setName(user_info_map.get("nickname"));
        xcUser.setUtype("101001");  // 学生类型
        xcUser.setStatus("1");
        xcUser.setCreateTime(LocalDateTime.now());
        // 2.5 添加到数据库
        iXcUserService.save(xcUser);
        // 3. 添加用户信息到用户角色表
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        iXcRoleUserService.save(xcUserRole);
        return xcUser;
    }
}