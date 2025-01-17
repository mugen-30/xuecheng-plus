package com.xuecheng.auth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindPswDto {

    String cellphone;

    String email;
 
    String checkcodekey;
 
    String checkcode;
    @NotEmpty(message = "密码不能为空")
    String password;
    @NotEmpty(message = "确认密码不能为空")
    String confirmpwd;
}