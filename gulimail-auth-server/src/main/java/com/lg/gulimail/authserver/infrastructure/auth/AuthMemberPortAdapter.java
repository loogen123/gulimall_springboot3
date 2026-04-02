package com.lg.gulimail.authserver.infrastructure.auth;

import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.application.port.out.AuthMemberPort;
import com.lg.gulimail.authserver.feign.MemberFeignService;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import org.springframework.stereotype.Component;

@Component
public class AuthMemberPortAdapter implements AuthMemberPort {
    private final MemberFeignService memberFeignService;

    public AuthMemberPortAdapter(MemberFeignService memberFeignService) {
        this.memberFeignService = memberFeignService;
    }

    @Override
    public R login(UserLoginVo vo) {
        return memberFeignService.login(vo);
    }

    @Override
    public R register(UserRegisterVo vo) {
        return memberFeignService.register(vo);
    }

    @Override
    public R oauthLogin(SocialUser socialUser) {
        return memberFeignService.oauthLogin(socialUser);
    }
}
