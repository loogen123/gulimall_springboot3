package com.lg.gulimail.authserver.application.auth;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.authserver.application.port.out.AuthMemberPort;
import com.lg.gulimail.authserver.application.port.out.GithubOAuthPort;
import com.lg.gulimail.authserver.domain.auth.AuthDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubOAuthApplicationServiceTest {
    @Mock
    private AuthMemberPort authMemberPort;
    @Mock
    private GithubOAuthPort githubOAuthPort;
    @Mock
    private AuthDomainService authDomainService;
    @InjectMocks
    private GithubOAuthApplicationService githubOAuthApplicationService;

    @Test
    void githubLoginShouldFailWhenStateMismatch() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("github_oauth_state", "state-1");

        OAuthLoginResult result = githubOAuthApplicationService.githubLogin("code", "state-2", session, "u", "c1", "c2", "r");

        assertFalse(result.isSuccess());
        verify(githubOAuthPort, never()).requestAccessToken(any(), any(), any(), any(), any());
    }

    @Test
    void githubLoginShouldSucceedWhenOauthAndMemberSuccess() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("github_oauth_state", "state-1");
        when(githubOAuthPort.requestAccessToken("u", "c1", "c2", "code", "r")).thenReturn("{\"access_token\":\"token\"}");
        when(authDomainService.extractAccessToken("{\"access_token\":\"token\"}")).thenReturn("token");
        when(authMemberPort.oauthLogin(any())).thenReturn(R.ok().put("member", new MemberResponseVo()));

        OAuthLoginResult result = githubOAuthApplicationService.githubLogin("code", "state-1", session, "u", "c1", "c2", "r");

        assertTrue(result.isSuccess());
        assertNotNull(session.getAttribute(AuthServerConstant.LOGIN_USER));
    }
}
