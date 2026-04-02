package com.lg.gulimail.member.controller;

import com.lg.common.exception.BizCodeEnum;
import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.member.application.integration.MemberIntegrationApplicationService;
import com.lg.gulimail.member.domain.integration.MemberIntegrationMutationResult;
import com.lg.gulimail.member.domain.integration.MemberIntegrationQuoteResult;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.exception.PhoneExistException;
import com.lg.gulimail.member.exception.UsernameExistException;
import com.lg.gulimail.member.service.MemberService;
import com.lg.gulimail.member.vo.MemberLoginVo;
import com.lg.gulimail.member.vo.MemberRegisterVo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("member/member")
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberIntegrationApplicationService memberIntegrationApplicationService;

    @PostMapping("/register")
    public R register(@Valid @RequestBody MemberRegisterVo vo, BindingResult result) {
        log.info("会员服务注册请求: phone={}, username={}", vo.getPhone(), vo.getUserName());
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1));
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "数据校验失败").put("errors", errors);
        }
        try {
            memberService.register(vo);
        } catch (PhoneExistException e) {
            log.warn("注册失败，手机号已存在: phone={}", vo.getPhone());
            return R.error(15001, "手机号已存在");
        } catch (UsernameExistException e) {
            log.warn("注册失败，用户名已存在: username={}", vo.getUserName());
            return R.error(15002, "用户名已存在");
        }
        return R.ok();
    }
    @PostMapping("/login")
    public R login(@Valid @RequestBody MemberLoginVo vo, BindingResult result) {
        log.info("会员服务登录请求: account={}", vo.getLoginacct());
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1));
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "数据校验失败").put("errors", errors);
        }
        MemberEntity entity = memberService.login(vo);
        if (entity != null) {
            log.info("会员登录成功: userId={}, account={}", entity.getId(), vo.getLoginacct());
            return R.ok().put("member", entity);
        } else {
            log.warn("会员登录失败: account={}", vo.getLoginacct());
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION);
        }
    }
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {
        log.info("社交登录请求: uid={}", socialUser.getUid());
        if (socialUser == null || socialUser.getAccessToken() == null || socialUser.getAccessToken().trim().isEmpty()) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), "accessToken不能为空");
        }
        socialUser.setAccessToken(socialUser.getAccessToken().trim());
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            log.info("社交登录成功: userId={}, uid={}", entity.getId(), socialUser.getUid());
            return R.ok().put("member", entity);
        } else {
            log.warn("社交登录失败: uid={}", socialUser.getUid());
            return R.error("社交登录失败");
        }
    }

    @PostMapping("/internal/integration/quote")
    public R quoteIntegration(@RequestBody Map<String, Object> request) {
        MemberIntegrationQuoteResult result = memberIntegrationApplicationService.quote(request);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok().put("useIntegration", result.getUseIntegration()).put("integrationAmount", result.getIntegrationAmount());
    }

    @PostMapping("/internal/integration/deduct")
    public R deductIntegration(@RequestBody Map<String, Object> request) {
        MemberIntegrationMutationResult result = memberIntegrationApplicationService.deduct(request);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/internal/integration/revert")
    public R revertIntegration(@RequestBody Map<String, Object> request) {
        MemberIntegrationMutationResult result = memberIntegrationApplicationService.revert(request);
        if (!result.isSuccess()) {
            return R.error(result.getCode(), result.getMessage());
        }
        return R.ok();
    }
}
