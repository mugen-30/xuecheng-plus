package com.xuecheng.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.user.mapper.XcCompanyMapper;
import com.xuecheng.user.model.po.XcCompany;
import com.xuecheng.user.service.IXcCompanyService;
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
public class XcCompanyServiceImpl extends ServiceImpl<XcCompanyMapper, XcCompany> implements IXcCompanyService {

    @Resource
    private XcCompanyMapper xcCompanyMapper;

    @Override
    @Transactional
    public XcCompany updateCompany(String companyId, XcCompany xcCompany) {
        //判断是否修改的是当前用户所属机构
        if (!companyId.equals(xcCompany.getId())) {
            XueChengPlusException.cast("无权限修改");
        }
        xcCompanyMapper.updateById(xcCompany);
        return xcCompany;
    }
}
