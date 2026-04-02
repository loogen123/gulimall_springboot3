package com.lg.gulimail.member.controller;

import com.lg.common.utils.R;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.member.application.integration.MemberIntegrationApplicationService;
import com.lg.gulimail.member.domain.integration.MemberIntegrationMutationResult;
import com.lg.gulimail.member.domain.integration.MemberIntegrationQuoteResult;
import com.lg.gulimail.member.entity.MemberEntity;
import com.lg.gulimail.member.service.MemberService;
import com.lg.gulimail.member.vo.MemberLoginVo;
import com.lg.gulimail.member.vo.MemberRegisterVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;
    @Mock
    private MemberIntegrationApplicationService memberIntegrationApplicationService;

    @InjectMocks
    private MemberController memberController;

    @Test
    void registerShouldRejectWhenValidationFailed() {
        MemberRegisterVo vo = new MemberRegisterVo();
        BindingResult result = new BeanPropertyBindingResult(vo, "vo");
        result.rejectValue("phone", "Pattern", "手机号格式错误");

        R response = memberController.register(vo, result);

        assertEquals(10001, response.getCode());
        verify(memberService, never()).register(vo);
    }

    @Test
    void loginShouldRejectWhenValidationFailed() {
        MemberLoginVo vo = new MemberLoginVo();
        BindingResult result = new BeanPropertyBindingResult(vo, "vo");
        result.rejectValue("loginacct", "NotBlank", "账号不能为空");

        R response = memberController.login(vo, result);

        assertEquals(10001, response.getCode());
        verify(memberService, never()).login(vo);
    }

    @Test
    void oauthLoginShouldRejectWhenAccessTokenBlank() throws Exception {
        SocialUser socialUser = new SocialUser();
        socialUser.setAccessToken(" ");

        R response = memberController.oauthLogin(socialUser);

        assertEquals(10001, response.getCode());
        verify(memberService, never()).login(socialUser);
    }

    @Test
    void oauthLoginShouldTrimAccessTokenBeforeCallService() throws Exception {
        SocialUser socialUser = new SocialUser();
        socialUser.setAccessToken(" token ");
        MemberEntity memberEntity = new MemberEntity();
        when(memberService.login(socialUser)).thenReturn(memberEntity);

        R response = memberController.oauthLogin(socialUser);

        assertEquals(0, response.getCode());
        assertEquals("token", socialUser.getAccessToken());
        verify(memberService).login(socialUser);
    }

    @Test
    void quoteIntegrationShouldReturnValidationErrorFromApplication() {
        MemberIntegrationQuoteResult result = MemberIntegrationQuoteResult.failure(10001, "请求参数不合法");
        when(memberIntegrationApplicationService.quote(null)).thenReturn(result);

        R response = memberController.quoteIntegration(null);

        assertEquals(10001, response.getCode());
        verify(memberIntegrationApplicationService).quote(null);
    }

    @Test
    void deductIntegrationShouldReturnSuccessWhenApplicationSuccess() {
        when(memberIntegrationApplicationService.deduct(null)).thenReturn(MemberIntegrationMutationResult.success());

        R response = memberController.deductIntegration(null);

        assertEquals(0, response.getCode());
        verify(memberIntegrationApplicationService).deduct(null);
    }
}
