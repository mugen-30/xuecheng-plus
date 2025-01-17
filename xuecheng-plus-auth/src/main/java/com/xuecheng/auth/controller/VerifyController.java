package com.xuecheng.auth.controller;

import com.xuecheng.auth.model.dto.FindPswDto;
import com.xuecheng.auth.model.dto.RegisterDto;
import com.xuecheng.auth.service.VerifyService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@Slf4j
@RestController
public class VerifyController {

    @Resource
    VerifyService verifyService;

    @ApiOperation(value = "找回密码", tags = "找回密码")
    @PostMapping("/findpassword")
    public void findPassword(@RequestBody @Validated FindPswDto findPswDto) {
        verifyService.findPassword(findPswDto);
    }

    @ApiOperation(value = "注册", tags = "注册")
    @PostMapping("/register")
    public void register(@RequestBody @Validated RegisterDto registerDto){
        verifyService.register(registerDto);
    }


}
