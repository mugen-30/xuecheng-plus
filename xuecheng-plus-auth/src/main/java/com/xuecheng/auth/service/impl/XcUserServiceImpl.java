package com.xuecheng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.auth.mapper.XcUserMapper;
import com.xuecheng.auth.model.po.XcUser;
import com.xuecheng.auth.service.IXcUserService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
public class XcUserServiceImpl extends ServiceImpl<XcUserMapper, XcUser> implements IXcUserService {

    @Override
    public PageResult<XcUser> queryUserList(Long companyId, PageParams pageParams) {

        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getCompanyId, companyId);
        Page<XcUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<XcUser> pageResult = baseMapper.selectPage(page, queryWrapper);
        //数据
        List<XcUser> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //封装返回结果
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

    }
}
