package com.lg.gulimail.thirdparty.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private AliyunSmsService aliyunSmsService;

    /**
     * 发送验证码（适配免资质签名模板 100001）
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 调用发送逻辑
        boolean isSuccess = aliyunSmsService.sendVerifyCode(phone);
        return isSuccess ? R.ok() : R.error("短信发送失败，请检查阿里云余额或配置");
    }

    /**
     * 校验验证码
     */
    @PostMapping("/checkCode")
    public R checkCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        boolean isCorrect = aliyunSmsService.checkVerifyCode(phone, code);
        return isCorrect ? R.ok() : R.error("验证码错误或已过期");
    }
}