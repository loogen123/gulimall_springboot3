package com.lg.common.constant;

public class AuthServerConstant {

    /**
     * 验证码前缀，存入 Redis 时使用 (例如: sms:code:13812345678)
     */
    public static final String SMS_CODE_CACHE_PREFIX = "sms:code:";

    /**
     * 登录用户在 Session 中存储的 Key
     */
    public static final String LOGIN_USER = "loginUser";
}