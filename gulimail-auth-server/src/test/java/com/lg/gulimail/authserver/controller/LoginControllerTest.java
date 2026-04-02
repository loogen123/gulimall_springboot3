package com.lg.gulimail.authserver.controller;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.application.auth.LoginApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginApplicationService loginApplicationService;

    @InjectMocks
    private LoginController loginController;

    @Test
    void loginPageShouldRedirectWhenUserAlreadyLoggedIn() {
        ReflectionTestUtils.setField(loginController, "loginSuccessRedirect", "http://gulimail.com");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthServerConstant.LOGIN_USER, new MemberResponseVo());

        String view = loginController.loginPage(session, new ConcurrentModel());

        assertEquals("redirect:http://gulimail.com", view);
    }

    @Test
    void loginPageShouldGenerateGithubAuthUrlAndStateWhenAnonymous() {
        ReflectionTestUtils.setField(loginController, "githubAuthorizeUrl", "https://github.com/login/oauth/authorize");
        ReflectionTestUtils.setField(loginController, "githubClientId", "cid");
        ReflectionTestUtils.setField(loginController, "githubRedirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        MockHttpSession session = new MockHttpSession();
        ConcurrentModel model = new ConcurrentModel();
        when(loginApplicationService.prepareGithubAuthUrl(session, "https://github.com/login/oauth/authorize", "cid", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn("https://github.com/login/oauth/authorize?client_id=cid&state=abc");

        String view = loginController.loginPage(session, model);

        assertEquals("login", view);
        String githubAuthUrl = (String) model.getAttribute("githubAuthUrl");
        assertNotNull(githubAuthUrl);
        assertTrue(githubAuthUrl.contains("client_id=cid"));
        verify(loginApplicationService).prepareGithubAuthUrl(session, "https://github.com/login/oauth/authorize", "cid", "http://auth.gulimail.com/oauth2.0/github/success");
    }

    @Test
    void sendCodeShouldRejectWhenPhoneInvalid() {
        when(loginApplicationService.sendCode("123")).thenReturn(R.error(10001, "手机号格式错误"));
        R result = loginController.sendCode("123");

        assertEquals(10001, result.getCode());
        verify(loginApplicationService).sendCode("123");
    }

    @Test
    void sendCodeShouldTrimPhoneBeforeRemoteCall() {
        when(loginApplicationService.sendCode(" 13800138000 ")).thenReturn(R.ok());

        R result = loginController.sendCode(" 13800138000 ");

        assertEquals(0, result.getCode());
        verify(loginApplicationService).sendCode(" 13800138000 ");
    }

    @Test
    void loginShouldRejectWhenValidationError() {
        UserLoginVo vo = new UserLoginVo();
        BindingResult bindingResult = new BeanPropertyBindingResult(vo, "vo");
        bindingResult.rejectValue("loginacct", "NotBlank", "用户名不能为空");
        MockHttpSession session = new MockHttpSession();
        when(loginApplicationService.login(vo, bindingResult, session)).thenReturn(R.error(400, "bad"));

        R result = loginController.login(vo, bindingResult, session);

        assertEquals(400, result.getCode());
        verify(loginApplicationService).login(vo, bindingResult, session);
    }

    @Test
    void loginShouldSetSessionWhenSuccess() {
        UserLoginVo vo = new UserLoginVo();
        vo.setLoginacct("u1");
        vo.setPassword("p1");
        BindingResult bindingResult = new BeanPropertyBindingResult(vo, "vo");
        MockHttpSession session = new MockHttpSession();
        when(loginApplicationService.login(vo, bindingResult, session)).thenReturn(R.ok().put("member", new MemberResponseVo()));

        R result = loginController.login(vo, bindingResult, session);

        assertEquals(0, result.getCode());
        verify(loginApplicationService).login(vo, bindingResult, session);
    }
}
