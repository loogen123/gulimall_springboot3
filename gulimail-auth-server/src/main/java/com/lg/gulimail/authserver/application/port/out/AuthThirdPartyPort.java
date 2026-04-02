package com.lg.gulimail.authserver.application.port.out;

import com.lg.common.utils.R;

public interface AuthThirdPartyPort {
    R sendCode(String phone);

    R checkCode(String phone, String code);
}
