package com.lg.gulimail.member.controller;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.exception.PhoneExistException;
import com.lg.gulimail.member.exception.UsernameExistException;
import com.lg.gulimail.member.service.MemberService;
import com.lg.gulimail.member.vo.MemberLoginVo;
import com.lg.gulimail.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            memberService.register(vo);
        } catch (PhoneExistException e) {
            return R.error(15001, "手机号已存在");
        } catch (UsernameExistException e) {
            return R.error(15002, "用户名已存在");
        }
        return R.ok();
    }
    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            return R.ok().put("member", entity);
        } else {
            return R.error(
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            return R.ok().put("member", entity);
        } else {
            return R.error("社交登录失败");
        }
    }
}