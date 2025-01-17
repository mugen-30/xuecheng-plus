package com.xuecheng.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.user.mapper.XcUserMapper;
import com.xuecheng.user.model.po.XcUser;
import com.xuecheng.user.service.IXcUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Resource
    private XcUserMapper xcUserMapper;

    @Override
    public PageResult<XcUser> queryUserList(Long companyId, PageParams pageParams) {

        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getCompanyId, companyId);
        Page<XcUser> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<XcUser> pageResult = xcUserMapper.selectPage(page, queryWrapper);
        //数据
        List<XcUser> items = pageResult.getRecords();
        //总记录数
        long total = pageResult.getTotal();

        //封装返回结果
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());

    }
}
