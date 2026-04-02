package com.lg.gulimail.thirdparty.domain.sms;

import org.springframework.stereotype.Service;

@Service
public class SmsDomainService {
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String CODE_REGEX = "^\\d{6}$";

    public SmsSendCommand normalizeSend(String phone) {
        SmsSendCommand command = new SmsSendCommand();
        command.setPhone(phone == null ? null : phone.trim());
        return command;
    }

    public SmsCheckCommand normalizeCheck(String phone, String code) {
        SmsCheckCommand command = new SmsCheckCommand();
        command.setPhone(phone == null ? null : phone.trim());
        command.setCode(code == null ? null : code.trim());
        return command;
    }

    public SmsResult validateSend(SmsSendCommand command) {
        if (command == null || command.getPhone() == null || !command.getPhone().matches(PHONE_REGEX)) {
            return SmsResult.invalidPhone();
        }
        return SmsResult.ok();
    }

    public SmsResult validateCheck(SmsCheckCommand command) {
        if (command == null || command.getPhone() == null || !command.getPhone().matches(PHONE_REGEX)) {
            return SmsResult.invalidPhone();
        }
        if (command.getCode() == null || !command.getCode().matches(CODE_REGEX)) {
            return SmsResult.invalidCode();
        }
        return SmsResult.ok();
    }
}
