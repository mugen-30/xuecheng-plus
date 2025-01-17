package com.xuecheng.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.user.mapper.XcMenuMapper;
import com.xuecheng.user.model.po.XcMenu;
import com.xuecheng.user.service.IXcMenuService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-01-07
 */
@Service
public class XcMenuServiceImpl extends ServiceImpl<XcMenuMapper, XcMenu> implements IXcMenuService {
    public List<XcMenu> selectPermissionByUserId(String userId) {
        return baseMapper.selectPermissionByUserId(userId);
    }
}
