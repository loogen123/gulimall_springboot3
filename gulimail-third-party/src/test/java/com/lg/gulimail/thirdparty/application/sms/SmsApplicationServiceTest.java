package com.lg.gulimail.thirdparty.application.sms;

import com.lg.gulimail.thirdparty.application.port.out.SmsPort;
import com.lg.gulimail.thirdparty.domain.sms.SmsDomainService;
import com.lg.gulimail.thirdparty.domain.sms.SmsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsApplicationServiceTest {
    @Mock
    private SmsPort smsPort;

    private SmsApplicationService smsApplicationService;

    @BeforeEach
    void setUp() {
        smsApplicationService = new SmsApplicationService(smsPort, new SmsDomainService());
    }

    @Test
    void sendCodeShouldRejectInvalidPhone() {
        SmsResult result = smsApplicationService.sendCode("123");
        assertEquals(10001, result.getCode());
        verify(smsPort, never()).sendVerifyCode("123");
    }

    @Test
    void sendCodeShouldReturnOkWhenPortSuccess() {
        when(smsPort.sendVerifyCode("13800138000")).thenReturn(true);
        SmsResult result = smsApplicationService.sendCode("13800138000");
        assertTrue(result.isSuccess());
        verify(smsPort).sendVerifyCode("13800138000");
    }

    @Test
    void checkCodeShouldCallPortWithNormalizedInput() {
        when(smsPort.checkVerifyCode("13800138000", "123456")).thenReturn(true);
        SmsResult result = smsApplicationService.checkCode(" 13800138000 ", " 123456 ");
        assertTrue(result.isSuccess());
        verify(smsPort).checkVerifyCode("13800138000", "123456");
    }
}
