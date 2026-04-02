package com.lg.gulimail.thirdparty.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliyunSmsServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AliyunSmsServiceImpl aliyunSmsService;

    @Test
    void sendVerifyCodeShouldReturnFalseWhenRequestTooFrequent() {
        when(redisTemplate.hasKey("sms:send:13800138000")).thenReturn(true);

        assertThrows(com.lg.common.utils.RRException.class, () -> aliyunSmsService.sendVerifyCode("13800138000"));
    }

    @Test
    void checkVerifyCodeShouldReturnTrueAndDeleteWhenMatch() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("sms:check:fail:13800138000")).thenReturn(null);
        when(valueOperations.get("sms:code:13800138000")).thenReturn("123456");

        boolean result = aliyunSmsService.checkVerifyCode("13800138000", "123456");

        assertTrue(result);
        verify(redisTemplate).delete("sms:code:13800138000");
        verify(redisTemplate).delete("sms:check:fail:13800138000");
    }

    @Test
    void checkVerifyCodeShouldReturnFalseWhenFailTimesExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("sms:check:fail:13800138000")).thenReturn("5");

        boolean result = aliyunSmsService.checkVerifyCode("13800138000", "123456");

        assertFalse(result);
        verify(redisTemplate).delete("sms:code:13800138000");
    }

    @Test
    void checkVerifyCodeShouldIncreaseFailCountWhenMismatch() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("sms:check:fail:13800138000")).thenReturn("0");
        when(valueOperations.get("sms:code:13800138000")).thenReturn("654321");

        boolean result = aliyunSmsService.checkVerifyCode("13800138000", "123456");

        assertFalse(result);
        verify(valueOperations).increment("sms:check:fail:13800138000");
        verify(redisTemplate).expire(eq("sms:check:fail:13800138000"), anyLong(), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    @Test
    void checkVerifyCodeShouldReturnFalseWhenPhoneOrCodeBlank() {
        boolean result1 = aliyunSmsService.checkVerifyCode(" ", "123456");
        boolean result2 = aliyunSmsService.checkVerifyCode("13800138000", " ");

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    void checkVerifyCodeShouldTreatNegativeFailCountAsZero() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("sms:check:fail:13800138000")).thenReturn("-3");
        when(valueOperations.get("sms:code:13800138000")).thenReturn("000000");

        boolean result = aliyunSmsService.checkVerifyCode("13800138000", "123456");

        assertFalse(result);
        verify(valueOperations).increment("sms:check:fail:13800138000");
    }

    @Test
    void generateVerifyCodeShouldReturnSixDigits() {
        String code = (String) ReflectionTestUtils.invokeMethod(aliyunSmsService, "generateVerifyCode");
        assertTrue(code.matches("^\\d{6}$"));
    }
}
