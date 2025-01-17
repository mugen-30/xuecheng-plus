package com.xuecheng.checkcode.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.service.RateLimitingService;
import com.xuecheng.checkcode.service.SendCodeService;
import com.xuecheng.checkcode.utils.MailUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(value = "发送验证码接口")
@RestController
public class SendCodeController {
    @Resource
    SendCodeService sendCodeService;
    @Resource
    RateLimitingService rateLimitingService;

    @ApiOperation(value = "发送邮箱验证码", tags = "发送邮箱验证码")
    @PostMapping("/email")
    public void sendEmailCode(@RequestParam("param1") String email) {
        if (!rateLimitingService.isAllowed(email, 1L)) {
            XueChengPlusException.cast("操作过于频繁，请稍后再试");
        }
        String code = MailUtil.achieveCode();
        sendCodeService.sendEMail(email, code);
    }

}
