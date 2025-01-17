package com.xuecheng.auth.service;

import com.xuecheng.auth.model.dto.FindPswDto;
import com.xuecheng.auth.model.dto.RegisterDto;

public interface VerifyService {
    void findPassword(FindPswDto findPswDto);

    void register(RegisterDto registerDto);
}