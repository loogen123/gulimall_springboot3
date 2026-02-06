package com.lg.gulimail.thirdparty.service;

public interface AliyunSmsService {
    boolean sendVerifyCode(String phone); // 修改方法签名
    boolean checkVerifyCode(String phone, String code);
}
