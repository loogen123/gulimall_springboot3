package com.lg.gulimail.member.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberLoginVo {

    /**
     * 用户名、手机号、或邮箱
     */
    @NotBlank(message = "账号不能为空")
    private String loginacct;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
