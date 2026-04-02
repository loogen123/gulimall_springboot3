package com.lg.gulimail.thirdparty.infrastructure.sms;

import com.lg.gulimail.thirdparty.application.port.out.SmsPort;
import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class SmsPortAdapter implements SmsPort {
    private final AliyunSmsService aliyunSmsService;

    public SmsPortAdapter(ObjectProvider<AliyunSmsService> aliyunSmsServiceProvider) {
        this.aliyunSmsService = aliyunSmsServiceProvider.getIfAvailable();
    }

    @Override
    public boolean sendVerifyCode(String phone) {
        if (aliyunSmsService == null) {
            return false;
        }
        return aliyunSmsService.sendVerifyCode(phone);
    }

    @Override
    public boolean checkVerifyCode(String phone, String code) {
        if (aliyunSmsService == null) {
            return false;
        }
        return aliyunSmsService.checkVerifyCode(phone, code);
    }
}
