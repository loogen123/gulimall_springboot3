package com.lg.gulimail.thirdparty.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.thirdparty.application.sms.SmsApplicationService;
import com.lg.gulimail.thirdparty.domain.sms.SmsResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsControllerTest {

    @Mock
    private SmsApplicationService smsApplicationService;

    @InjectMocks
    private SmsController smsController;

    @Test
    void sendCodeShouldRejectInvalidPhone() {
        when(smsApplicationService.sendCode("123")).thenReturn(SmsResult.invalidPhone());
        R result = smsController.sendCode("123");
        assertEquals(10001, result.getCode());
        verify(smsApplicationService).sendCode("123");
    }

    @Test
    void sendCodeShouldReturnOkWhenServiceSuccess() {
        when(smsApplicationService.sendCode("13800138000")).thenReturn(SmsResult.ok());
        R result = smsController.sendCode("13800138000");
        assertEquals(0, result.getCode());
    }

    @Test
    void sendCodeShouldReturnErrorWhenServiceFailed() {
        when(smsApplicationService.sendCode("13800138000")).thenReturn(SmsResult.failed("短信发送失败"));
        R result = smsController.sendCode("13800138000");
        assertEquals(500, result.getCode());
    }

    @Test
    void checkCodeShouldRejectInvalidCodeFormat() {
        when(smsApplicationService.checkCode("13800138000", "a1")).thenReturn(SmsResult.invalidCode());
        R result = smsController.checkCode("13800138000", "a1");
        assertEquals(10001, result.getCode());
    }

    @Test
    void checkCodeShouldRejectInvalidPhoneFormat() {
        when(smsApplicationService.checkCode("12", "123456")).thenReturn(SmsResult.invalidPhone());
        R result = smsController.checkCode("12", "123456");
        assertEquals(10001, result.getCode());
        verify(smsApplicationService).checkCode("12", "123456");
    }

    @Test
    void checkCodeShouldReturnOkWhenCodeCorrect() {
        when(smsApplicationService.checkCode("13800138000", "123456")).thenReturn(SmsResult.ok());

        R result = smsController.checkCode("13800138000", "123456");

        assertEquals(0, result.getCode());
    }

    @Test
    void checkCodeShouldTrimInputsBeforeCallService() {
        when(smsApplicationService.checkCode(" 13800138000 ", " 123456 ")).thenReturn(SmsResult.failed("验证码错误或已过期"));

        R result = smsController.checkCode(" 13800138000 ", " 123456 ");

        assertEquals(500, result.getCode());
        verify(smsApplicationService).checkCode(" 13800138000 ", " 123456 ");
    }
}
