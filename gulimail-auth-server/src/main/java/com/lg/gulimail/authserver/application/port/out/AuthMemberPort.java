package com.lg.gulimail.authserver.application.port.out;

import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.vo.UserRegisterVo;

public interface AuthMemberPort {
    R login(UserLoginVo vo);

    R register(UserRegisterVo vo);

    R oauthLogin(SocialUser socialUser);
}
