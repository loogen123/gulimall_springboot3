package com.lg.common.utils;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logback 脱敏转换器
 * 用于在日志输出时自动对手机号、邮箱等敏感信息进行脱敏
 */
public class LogDesensitizeConverter extends ClassicConverter {

    /**
     * 手机号正则：匹配 1 开头，后面 10 位数字的 11 位号码
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("(1[3-9]\\d)\\d{4}(\\d{4})");

    /**
     * 邮箱正则：简单匹配
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w?)(\\w+)(@\\w+\\.[a-z]+)");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isEmpty()) {
            return message;
        }

        // 1. 脱敏手机号
        Matcher phoneMatcher = PHONE_PATTERN.matcher(message);
        if (phoneMatcher.find()) {
            message = phoneMatcher.replaceAll("$1****$2");
        }

        // 2. 脱敏邮箱
        Matcher emailMatcher = EMAIL_PATTERN.matcher(message);
        if (emailMatcher.find()) {
            message = emailMatcher.replaceAll("$1******$3");
        }

        return message;
    }
}
