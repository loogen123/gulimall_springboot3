package com.lg.gulimail.authserver.application.auth;

import com.alibaba.fastjson.TypeReference;
import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.common.vo.SocialUser;
import com.lg.gulimail.authserver.application.port.out.AuthMemberPort;
import com.lg.gulimail.authserver.application.port.out.GithubOAuthPort;
import com.lg.gulimail.authserver.domain.auth.AuthDomainService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GithubOAuthApplicationService {
    private static final String GITHUB_OAUTH_STATE = "github_oauth_state";
    private final AuthMemberPort authMemberPort;
    private final GithubOAuthPort githubOAuthPort;
    private final AuthDomainService authDomainService;

    public GithubOAuthApplicationService(AuthMemberPort authMemberPort, GithubOAuthPort githubOAuthPort, AuthDomainService authDomainService) {
        this.authMemberPort = authMemberPort;
        this.githubOAuthPort = githubOAuthPort;
        this.authDomainService = authDomainService;
    }

    public OAuthLoginResult githubLogin(String code, String state, HttpSession session, String accessTokenUrl, String clientId, String clientSecret, String redirectUri) {
        Object expectedState = session.getAttribute(GITHUB_OAUTH_STATE);
        session.removeAttribute(GITHUB_OAUTH_STATE);
        if (expectedState == null || !expectedState.equals(state)) {
            return OAuthLoginResult.failed();
        }
        if (!StringUtils.hasText(code)) {
            return OAuthLoginResult.failed();
        }
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            return OAuthLoginResult.failed();
        }
        String body = githubOAuthPort.requestAccessToken(accessTokenUrl, clientId, clientSecret, code.trim(), redirectUri);
        String accessToken = authDomainService.extractAccessToken(body);
        if (!StringUtils.hasText(accessToken)) {
            return OAuthLoginResult.failed();
        }
        SocialUser socialUser = new SocialUser();
        socialUser.setAccessToken(accessToken);
        R memberResult = authMemberPort.oauthLogin(socialUser);
        if (memberResult.getCode() != 0) {
            return OAuthLoginResult.failed();
        }
        MemberResponseVo member = memberResult.getData("member", new TypeReference<MemberResponseVo>() {
        });
        if (member == null) {
            return OAuthLoginResult.failed();
        }
        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
        return OAuthLoginResult.success();
    }
}
