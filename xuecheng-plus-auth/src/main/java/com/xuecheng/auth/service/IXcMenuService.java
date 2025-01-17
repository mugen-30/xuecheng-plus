package com.xuecheng.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.auth.model.po.XcMenu;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-01-07
 */
public interface IXcMenuService extends IService<XcMenu> {

    List<XcMenu> selectPermissionByUserId(String id);
}
