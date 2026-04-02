package com.lg.gulimail.thirdparty.domain.sms;

import lombok.Data;

@Data
public class SmsCheckCommand {
    private String phone;
    private String code;
}
