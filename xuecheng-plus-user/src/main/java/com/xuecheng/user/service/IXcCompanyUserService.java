package com.xuecheng.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.user.model.po.XcCompanyUser;
import com.xuecheng.user.model.po.XcUser;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-01-07
 */
public interface IXcCompanyUserService extends IService<XcCompanyUser> {

    XcUser binding(Long companyId, String userId, Boolean isAdmin);

    void unbinding(Long companyId, String userId);
}
