package com.xuecheng.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.user.mapper.XcCompanyUserMapper;
import com.xuecheng.user.model.po.XcCompanyUser;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.model.po.XcUserRole;
import com.xuecheng.user.service.IXcCompanyUserService;
import com.xuecheng.user.service.IXcUserRoleService;
import com.xuecheng.user.service.IXcUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-01-07
 */
@Service
public class XcCompanyUserServiceImpl extends ServiceImpl<XcCompanyUserMapper, XcCompanyUser> implements IXcCompanyUserService {

    @Resource
    private IXcUserService iXcUserService;

    @Resource
    private XcCompanyUserMapper xcCompanyUserMapper;

    @Resource
    private IXcUserRoleService iXcUserRoleService;

    @Override
    @Transactional
    public XcUser binding(Long companyId, String userId, Boolean isAdmin) {


        XcUser user = iXcUserService.getById(userId);
        if (user == null) {
            XueChengPlusException.cast("用户不存在");
        }
        XcCompanyUser xcCompanyUser = xcCompanyUserMapper.selectOne(new QueryWrapper<XcCompanyUser>().lambda().eq(XcCompanyUser::getUserId, userId));
        if (xcCompanyUser != null) {
            XueChengPlusException.cast("用户已绑定机构");
        }
        XcCompanyUser xcCompanyUserNew = new XcCompanyUser();
        xcCompanyUserNew.setCompanyId(String.valueOf(companyId));
        xcCompanyUserNew.setUserId(user.getId());
        xcCompanyUserMapper.insert(xcCompanyUserNew);

        // 更新用户角色
        XcUserRole xcUserRole = iXcUserRoleService.getOne(new QueryWrapper<XcUserRole>().lambda().eq(XcUserRole::getUserId, user.getId()));
        if (isAdmin) {
            xcUserRole.setRoleId("20");
        } else {
            xcUserRole.setRoleId("18");
        }
        boolean b = iXcUserRoleService.updateById(xcUserRole);
        if (!b) {
            log.error("更新用户角色失败");
            XueChengPlusException.cast("绑定失败");
        }

        // 更新用户信息
        user.setCompanyId(String.valueOf(companyId));
        user.setUtype("101002");
        boolean b1 = iXcUserService.updateById(user);
        if (!b1) {
            log.error("更新用户信息失败");
            XueChengPlusException.cast("绑定失败");
        }

        return user;
    }

    @Override
    @Transactional
    public void unbinding(Long companyId, String userId) {
        XcCompanyUser xcCompanyUser = xcCompanyUserMapper.selectOne(new QueryWrapper<XcCompanyUser>().lambda().eq(XcCompanyUser::getUserId, userId));
        if (xcCompanyUser == null) {
            XueChengPlusException.cast("用户未绑定机构");
        }
        xcCompanyUserMapper.deleteById(xcCompanyUser.getId());
        // 更新用户角色
        XcUserRole xcUserRole = iXcUserRoleService.getOne(new QueryWrapper<XcUserRole>().lambda().eq(XcUserRole::getUserId, userId));
        xcUserRole.setRoleId("17");
        boolean b1 = iXcUserRoleService.updateById(xcUserRole);
        if (!b1) {
            log.error("更新用户角色失败");
            XueChengPlusException.cast("解绑失败");
        }
        // 更新用户信息
        XcUser user = iXcUserService.getById(userId);
        user.setCompanyId("");
        user.setUtype("101001");
        boolean b = iXcUserService.updateById(user);
        if (!b) {
            log.error("更新用户信息失败");
            XueChengPlusException.cast("解绑失败");
        }
    }
}
