package com.lg.gulimail.authserver.infrastructure.auth;

import com.lg.common.utils.R;
import com.lg.gulimail.authserver.application.port.out.AuthThirdPartyPort;
import com.lg.gulimail.authserver.feign.ThirdPartyFeignService;
import org.springframework.stereotype.Component;

@Component
public class AuthThirdPartyPortAdapter implements AuthThirdPartyPort {
    private final ThirdPartyFeignService thirdPartyFeignService;

    public AuthThirdPartyPortAdapter(ThirdPartyFeignService thirdPartyFeignService) {
        this.thirdPartyFeignService = thirdPartyFeignService;
    }

    @Override
    public R sendCode(String phone) {
        return thirdPartyFeignService.sendCode(phone);
    }

    @Override
    public R checkCode(String phone, String code) {
        return thirdPartyFeignService.checkCode(phone, code);
    }
}
