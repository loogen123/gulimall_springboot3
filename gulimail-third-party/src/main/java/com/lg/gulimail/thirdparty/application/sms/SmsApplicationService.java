package com.lg.gulimail.thirdparty.application.sms;

import com.lg.gulimail.thirdparty.application.port.out.SmsPort;
import com.lg.gulimail.thirdparty.domain.sms.SmsCheckCommand;
import com.lg.gulimail.thirdparty.domain.sms.SmsDomainService;
import com.lg.gulimail.thirdparty.domain.sms.SmsResult;
import com.lg.gulimail.thirdparty.domain.sms.SmsSendCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(SmsPort.class)
public class SmsApplicationService {
    private final SmsPort smsPort;
    private final SmsDomainService smsDomainService;

    public SmsApplicationService(SmsPort smsPort, SmsDomainService smsDomainService) {
        this.smsPort = smsPort;
        this.smsDomainService = smsDomainService;
    }

    public SmsResult sendCode(String phone) {
        SmsSendCommand command = smsDomainService.normalizeSend(phone);
        SmsResult validateResult = smsDomainService.validateSend(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        boolean success = smsPort.sendVerifyCode(command.getPhone());
        return success ? SmsResult.ok() : SmsResult.failed("短信发送失败");
    }

    public SmsResult checkCode(String phone, String code) {
        SmsCheckCommand command = smsDomainService.normalizeCheck(phone, code);
        SmsResult validateResult = smsDomainService.validateCheck(command);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        boolean success = smsPort.checkVerifyCode(command.getPhone(), command.getCode());
        return success ? SmsResult.ok() : SmsResult.failed("验证码错误或已过期");
    }
}
