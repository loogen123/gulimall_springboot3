package com.lg.gulimail.thirdparty.domain.sms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsDomainServiceTest {
    private final SmsDomainService smsDomainService = new SmsDomainService();

    @Test
    void validateSendShouldRejectInvalidPhone() {
        SmsSendCommand command = smsDomainService.normalizeSend("12");
        SmsResult result = smsDomainService.validateSend(command);
        assertEquals(10001, result.getCode());
    }

    @Test
    void validateCheckShouldRejectInvalidCode() {
        SmsCheckCommand command = smsDomainService.normalizeCheck("13800138000", "12a");
        SmsResult result = smsDomainService.validateCheck(command);
        assertEquals(10001, result.getCode());
    }

    @Test
    void validateCheckShouldPassWhenInputValid() {
        SmsCheckCommand command = smsDomainService.normalizeCheck("13800138000", "123456");
        SmsResult result = smsDomainService.validateCheck(command);
        assertTrue(result.isSuccess());
    }
}
