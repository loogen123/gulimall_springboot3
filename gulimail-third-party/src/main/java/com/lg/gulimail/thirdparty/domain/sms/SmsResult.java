package com.lg.gulimail.thirdparty.domain.sms;

import com.lg.common.exception.BizCodeEnum;
import lombok.Data;

@Data
public class SmsResult {
    private Integer code;
    private String message;

    public static SmsResult ok() {
        SmsResult result = new SmsResult();
        result.setCode(0);
        result.setMessage("success");
        return result;
    }

    public static SmsResult invalidPhone() {
        SmsResult result = new SmsResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("手机号格式错误");
        return result;
    }

    public static SmsResult invalidCode() {
        SmsResult result = new SmsResult();
        result.setCode(BizCodeEnum.VAILD_EXCEPTION.getCode());
        result.setMessage("验证码格式错误");
        return result;
    }

    public static SmsResult failed(String message) {
        SmsResult result = new SmsResult();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
