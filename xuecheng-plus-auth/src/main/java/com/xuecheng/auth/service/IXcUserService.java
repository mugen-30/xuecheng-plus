package com.xuecheng.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.auth.model.po.XcUser;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-01-07
 */
public interface IXcUserService extends IService<XcUser> {

    PageResult<XcUser> queryUserList(Long companyId, PageParams pageParams);
}
