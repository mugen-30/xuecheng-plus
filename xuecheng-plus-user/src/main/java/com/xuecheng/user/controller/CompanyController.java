package com.xuecheng.user.controller;


import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.user.model.po.XcCompany;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.IXcCompanyService;
import com.xuecheng.user.service.IXcCompanyUserService;
import com.xuecheng.user.service.IXcUserService;
import com.xuecheng.user.util.SecurityUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(value = "机构管理", tags = "机构管理")
@RestController
@Slf4j
public class CompanyController {

    @Resource
    private IXcUserService iXcUserService;

    @Resource
    private IXcCompanyService iXcCompanyService;

    @Resource
    private IXcCompanyUserService iXcCompanyUserService;

    @PostMapping("/member/list")
    public PageResult<XcUser> list(PageParams pageParams) {

        //获取当前用户所属机构ID
        Long companyId = Long.valueOf(SecurityUtil.getUser().getCompanyId());
        log.info("companyId:{}", companyId);

        return iXcUserService.queryUserList(companyId, pageParams);
    }

    @GetMapping("/my-company")
    public XcCompany getMyCompany() {

        //获取当前用户所属机构ID
        Long companyId = Long.valueOf(SecurityUtil.getUser().getCompanyId());

        return iXcCompanyService.getById(companyId);
    }

    @GetMapping("/member/binding")
    public XcUser BindingByUsername(@RequestParam String userId, @RequestParam Boolean isAdmin) {

        //获取当前用户所属机构ID
        Long companyId = Long.valueOf(SecurityUtil.getUser().getCompanyId());

        return iXcCompanyUserService.binding(companyId, userId, isAdmin);
    }

    @PutMapping("/company")
    public XcCompany updateCompany(@RequestBody XcCompany xcCompany) {

        //获取当前用户所属机构ID
        String companyId = SecurityUtil.getUser().getCompanyId();

        return iXcCompanyService.updateCompany(companyId, xcCompany);
    }

    @PostMapping("/member/unbinding/{userId}")
    public void unbinding(@PathVariable String userId) {

        //获取当前用户所属机构ID
        Long companyId = Long.valueOf(SecurityUtil.getUser().getCompanyId());

        iXcCompanyUserService.unbinding(companyId, userId);
    }

}
