package com.lg.gulimail.authserver.controller;

import com.lg.gulimail.authserver.application.auth.GithubOAuthApplicationService;
import com.lg.gulimail.authserver.application.auth.OAuthLoginResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ControllerTest {

    @Mock
    private GithubOAuthApplicationService githubOAuthApplicationService;

    @InjectMocks
    private OAuth2Controller oAuth2Controller;

    @Test
    void githubShouldRejectWhenStateInvalid() {
        ReflectionTestUtils.setField(oAuth2Controller, "loginPageUrl", "http://auth.gulimail.com/login.html");
        ReflectionTestUtils.setField(oAuth2Controller, "accessTokenUrl", "https://github.com/login/oauth/access_token");
        ReflectionTestUtils.setField(oAuth2Controller, "clientId", "cid");
        ReflectionTestUtils.setField(oAuth2Controller, "clientSecret", "secret");
        ReflectionTestUtils.setField(oAuth2Controller, "redirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        MockHttpSession session = new MockHttpSession();
        when(githubOAuthApplicationService.githubLogin("code", "wrong", session,
                "https://github.com/login/oauth/access_token", "cid", "secret", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn(OAuthLoginResult.failed());

        String view = oAuth2Controller.github("code", "wrong", session);

        assertEquals("redirect:http://auth.gulimail.com/login.html", view);
        verify(githubOAuthApplicationService).githubLogin("code", "wrong", session,
                "https://github.com/login/oauth/access_token", "cid", "secret", "http://auth.gulimail.com/oauth2.0/github/success");
    }

    @Test
    void githubShouldRejectWhenClientConfigMissing() {
        ReflectionTestUtils.setField(oAuth2Controller, "loginPageUrl", "http://auth.gulimail.com/login.html");
        ReflectionTestUtils.setField(oAuth2Controller, "accessTokenUrl", "https://github.com/login/oauth/access_token");
        ReflectionTestUtils.setField(oAuth2Controller, "clientId", "");
        ReflectionTestUtils.setField(oAuth2Controller, "clientSecret", "");
        ReflectionTestUtils.setField(oAuth2Controller, "redirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        MockHttpSession session = new MockHttpSession();
        when(githubOAuthApplicationService.githubLogin("code", "ok", session,
                "https://github.com/login/oauth/access_token", "", "", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn(OAuthLoginResult.failed());

        String view = oAuth2Controller.github("code", "ok", session);

        assertEquals("redirect:http://auth.gulimail.com/login.html", view);
    }

    @Test
    void githubShouldRejectWhenCodeBlank() {
        ReflectionTestUtils.setField(oAuth2Controller, "loginPageUrl", "http://auth.gulimail.com/login.html");
        ReflectionTestUtils.setField(oAuth2Controller, "accessTokenUrl", "https://github.com/login/oauth/access_token");
        ReflectionTestUtils.setField(oAuth2Controller, "clientId", "cid");
        ReflectionTestUtils.setField(oAuth2Controller, "clientSecret", "secret");
        ReflectionTestUtils.setField(oAuth2Controller, "redirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        MockHttpSession session = new MockHttpSession();
        when(githubOAuthApplicationService.githubLogin(" ", "ok", session,
                "https://github.com/login/oauth/access_token", "cid", "secret", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn(OAuthLoginResult.failed());

        String view = oAuth2Controller.github(" ", "ok", session);

        assertEquals("redirect:http://auth.gulimail.com/login.html", view);
    }

    @Test
    void githubShouldLoginWhenOauthAndMemberSuccess() {
        ReflectionTestUtils.setField(oAuth2Controller, "accessTokenUrl", "https://github.com/login/oauth/access_token");
        ReflectionTestUtils.setField(oAuth2Controller, "clientId", "cid");
        ReflectionTestUtils.setField(oAuth2Controller, "clientSecret", "secret");
        ReflectionTestUtils.setField(oAuth2Controller, "redirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        ReflectionTestUtils.setField(oAuth2Controller, "loginPageUrl", "http://auth.gulimail.com/login.html");
        ReflectionTestUtils.setField(oAuth2Controller, "loginSuccessRedirect", "http://gulimail.com");
        MockHttpSession session = new MockHttpSession();
        when(githubOAuthApplicationService.githubLogin("code", "ok", session,
                "https://github.com/login/oauth/access_token", "cid", "secret", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn(OAuthLoginResult.success());

        String view = oAuth2Controller.github("code", "ok", session);

        assertEquals("redirect:http://gulimail.com", view);
    }

    @Test
    void githubShouldRedirectLoginWhenMemberServiceFailed() {
        ReflectionTestUtils.setField(oAuth2Controller, "accessTokenUrl", "https://github.com/login/oauth/access_token");
        ReflectionTestUtils.setField(oAuth2Controller, "clientId", "cid");
        ReflectionTestUtils.setField(oAuth2Controller, "clientSecret", "secret");
        ReflectionTestUtils.setField(oAuth2Controller, "redirectUri", "http://auth.gulimail.com/oauth2.0/github/success");
        ReflectionTestUtils.setField(oAuth2Controller, "loginPageUrl", "http://auth.gulimail.com/login.html");
        MockHttpSession session = new MockHttpSession();
        when(githubOAuthApplicationService.githubLogin("code", "ok", session,
                "https://github.com/login/oauth/access_token", "cid", "secret", "http://auth.gulimail.com/oauth2.0/github/success"))
                .thenReturn(OAuthLoginResult.failed());

        String view = oAuth2Controller.github("code", "ok", session);

        assertEquals("redirect:http://auth.gulimail.com/login.html", view);
    }

}
