package com.lg.gulimail.thirdparty.application.port.out;

public interface SmsPort {
    boolean sendVerifyCode(String phone);

    boolean checkVerifyCode(String phone, String code);
}
