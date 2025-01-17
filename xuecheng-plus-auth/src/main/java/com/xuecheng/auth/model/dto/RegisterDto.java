package com.xuecheng.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    private String cellphone;

    private String checkcode;

    private String checkcodekey;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmpwd;

    private String email;

    private String nickname;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "用户名不能为空")
    private String username;

}