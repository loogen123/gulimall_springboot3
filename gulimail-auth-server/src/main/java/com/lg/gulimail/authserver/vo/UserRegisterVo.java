package com.lg.gulimail.authserver.vo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterVo {
    
    @NotEmpty(message = "用户名必须填写")
    @Length(min = 6, max = 18, message = "用户名长度在6-18位之间")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6, max = 18, message = "密码长度在6-18位之间")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1][3-9][0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}