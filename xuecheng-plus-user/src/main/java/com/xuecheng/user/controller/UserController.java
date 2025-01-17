package com.xuecheng.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.IXcUserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(value = "用户管理", tags = "用户管理")
@RestController
public class UserController {
    @Resource
    IXcUserService iXcUserService;

    @GetMapping("/search")
    public List<XcUser> search(@RequestParam String query) {
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>();
        queryWrapper.like(XcUser::getUsername, query);
        return iXcUserService.list(queryWrapper);
    }

}
