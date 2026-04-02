package com.lg.common.utils;

import org.springframework.util.StringUtils;

/**
 * 日志脱敏工具类
 */
public class LogDesensitizeUtils {

    /**
     * 手机号脱敏：13812345678 -> 138****5678
     */
    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 邮箱脱敏：example@gmail.com -> e******@gmail.com
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        int index = email.indexOf("@");
        if (index <= 1) {
            return "*@" + email.substring(index + 1);
        }
        return email.charAt(0) + "******" + email.substring(index);
    }

    /**
     * 密码脱敏：直接屏蔽
     */
    public static String maskPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return password;
        }
        return "******";
    }

    /**
     * 通用掩码处理
     */
    public static String mask(String text, int startInclude, int endExclude) {
        if (!StringUtils.hasText(text) || startInclude < 0 || endExclude > text.length() || startInclude >= endExclude) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        for (int i = startInclude; i < endExclude; i++) {
            sb.setCharAt(i, '*');
        }
        return sb.toString();
    }
}
