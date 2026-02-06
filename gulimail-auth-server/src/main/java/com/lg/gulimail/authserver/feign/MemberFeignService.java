package com.lg.gulimail.authserver.feign;

import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimail-member") // 对应注册在 Nacos 中的会员服务名
public interface MemberFeignService {

    /**
     * 调用会员服务进行注册
     * @param vo 包含注册信息的对象
     * @return 注册结果
     */
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser); // 必须加 @RequestBody
}