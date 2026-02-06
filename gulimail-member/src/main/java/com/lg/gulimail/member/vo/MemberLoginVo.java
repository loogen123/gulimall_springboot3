package com.lg.gulimail.member.vo;

import lombok.Data;

@Data
public class MemberLoginVo {

    /**
     * 用户名、手机号、或邮箱
     */
    private String loginacct;

    /**
     * 密码
     */
    private String password;
}