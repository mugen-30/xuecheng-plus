package com.xuecheng.auth.controller;

import com.xuecheng.auth.model.po.XcUser;
import com.xuecheng.auth.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Controller
public class WxLoginController {

    @Resource
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        XcUser user = wxAuthService.wxAuth(code);
        if(user==null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = user.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }
}