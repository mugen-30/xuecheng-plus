package com.xuecheng.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.model.dto.FindPswDto;
import com.xuecheng.auth.model.dto.RegisterDto;
import com.xuecheng.auth.model.po.XcUser;
import com.xuecheng.auth.model.po.XcUserRole;
import com.xuecheng.auth.service.IXcUserRoleService;
import com.xuecheng.auth.service.IXcUserService;
import com.xuecheng.auth.service.VerifyService;
import com.xuecheng.base.exception.XueChengPlusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VerifyServiceImpl implements VerifyService {

    private static final Logger log = LoggerFactory.getLogger(VerifyServiceImpl.class);
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    IXcUserService iXcUserService;

    @Resource
    IXcUserRoleService iXcUserRoleService;

    @Override
    public void findPassword(FindPswDto findPswDto) {
        String phone = findPswDto.getCellphone();
        String email = findPswDto.getEmail();
        if (phone == null && email == null) {
            XueChengPlusException.cast("请填入手机号或邮箱");
        }
        String checkcode = findPswDto.getCheckcode();
        //查找用户是否存在，如果不存在则抛出异常
        XcUser user1 = iXcUserService.getOne(new LambdaQueryWrapper<XcUser>()
                .eq(phone != null ? XcUser::getCellphone : XcUser::getEmail, phone != null ? phone : email));
        if (user1 == null) {
            XueChengPlusException.cast("用户不存在");
        }
        Boolean verify = verify(phone, email, checkcode);
        if (!verify) {
            XueChengPlusException.cast("验证码输入错误");
        }
        String password = findPswDto.getPassword();
        String confirmpwd = findPswDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengPlusException.cast("两次输入的密码不一致");
        }
        LambdaQueryWrapper<XcUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(XcUser::getEmail, findPswDto.getEmail());
        XcUser user = iXcUserService.getOne(lambdaQueryWrapper);
        if (user == null) {
            XueChengPlusException.cast("用户不存在");
        }
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        iXcUserService.updateById(user);
    }

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        String uuid = UUID.randomUUID().toString();
        String phone = registerDto.getCellphone();
        String email = registerDto.getEmail();
        String checkcode = registerDto.getCheckcode();
        //查找用户是否存在，如果存在则抛出异常
        // Check if the phone number is already registered
        if (phone != null && !phone.isEmpty()) {
            XcUser userByPhone = iXcUserService.getOne(new LambdaQueryWrapper<XcUser>()
                    .eq(XcUser::getCellphone, phone));
            if (userByPhone != null) {
                XueChengPlusException.cast("该手机号已被注册");
            }
        }
        // Check if the email is already registered
        if (email != null && !email.isEmpty()) {
            XcUser userByEmail = iXcUserService.getOne(new LambdaQueryWrapper<XcUser>()
                    .eq(XcUser::getEmail, email));
            if (userByEmail != null) {
                XueChengPlusException.cast("该邮箱已被注册");
            }
        }
        Boolean verify = verify(phone, email, checkcode);
        if (!verify) {
            XueChengPlusException.cast("验证码输入错误");
        }
        String password = registerDto.getPassword();
        String confirmpwd = registerDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengPlusException.cast("两次输入的密码不一致");
        }
        XcUser xcUser = new XcUser();
        BeanUtils.copyProperties(registerDto, xcUser);
        xcUser.setPassword(new BCryptPasswordEncoder().encode(password));
        xcUser.setId(uuid);
        xcUser.setUtype("101001");  // 学生类型
        xcUser.setStatus("1");
        xcUser.setName(registerDto.getNickname());
        xcUser.setCreateTime(LocalDateTime.now());
        if (!iXcUserService.save(xcUser)) {
            log.error("新增用户信息失败:{}", xcUser);
            XueChengPlusException.cast("注册失败");
        }
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(uuid);
        xcUserRole.setUserId(uuid);
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        if (!iXcUserRoleService.save(xcUserRole)) {
            log.error("新增用户角色信息失败:{}", xcUserRole);
            XueChengPlusException.cast("注册失败");
        }
    }

    private Boolean verify(String phone, String email, String checkcode) {
        //有手机号则用手机号验证，没有则用邮箱验证
        String check;
        if (!phone.isEmpty()) {
            check = stringRedisTemplate.opsForValue().get(phone);
        } else {
            check = stringRedisTemplate.opsForValue().get(email);
        }
        if (check == null) {
            return false;
        }
        //判断验证码是否正确，不分大小写
        return check.equalsIgnoreCase(checkcode);
    }
}