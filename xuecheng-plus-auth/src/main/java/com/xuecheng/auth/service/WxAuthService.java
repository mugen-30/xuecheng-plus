package com.xuecheng.auth.service;

import com.xuecheng.auth.model.po.XcUser;


public interface WxAuthService {

    public XcUser wxAuth(String code);

}