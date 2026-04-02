package com.lg.gulimail.thirdparty.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.thirdparty.application.sms.SmsApplicationService;
import com.lg.gulimail.thirdparty.domain.sms.SmsResult;
import com.lg.gulimail.thirdparty.service.AliyunSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@ConditionalOnBean(AliyunSmsService.class)
public class SmsController {
    private static final Logger log = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsApplicationService smsApplicationService;

    /**
     * 发送验证码（适配免资质签名模板 100001）
     */
    @RequestMapping(value = "/sendCode", method = {RequestMethod.GET, RequestMethod.POST})
    public R sendCode(@RequestParam("phone") String phone) {
        log.info("第三方服务发送短信: phone={}", phone);
        SmsResult result = smsApplicationService.sendCode(phone);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok();
    }

    /**
     * 校验验证码
     */
    @PostMapping("/checkCode")
    public R checkCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        log.info("第三方服务校验验证码: phone={}, code={}", phone, code);
        SmsResult result = smsApplicationService.checkCode(phone, code);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok();
    }
}
