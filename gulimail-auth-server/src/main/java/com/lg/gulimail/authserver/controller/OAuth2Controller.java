package com.lg.gulimail.authserver.controller;

import com.lg.gulimail.authserver.application.auth.GithubOAuthApplicationService;
import com.lg.gulimail.authserver.application.auth.OAuthLoginResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OAuth2Controller {
    private final GithubOAuthApplicationService githubOAuthApplicationService;

    @Value("${gulimail.auth.oauth.github.access-token-url:https://github.com/login/oauth/access_token}")
    private String accessTokenUrl;

    @Value("${gulimail.auth.oauth.github.client-id:}")
    private String clientId;

    @Value("${gulimail.auth.oauth.github.client-secret:}")
    private String clientSecret;

    @Value("${gulimail.auth.oauth.github.redirect-uri:http://auth.gulimail.com/oauth2.0/github/success}")
    private String redirectUri;

    @Value("${gulimail.auth.login-page-url:http://auth.gulimail.com/login.html}")
    private String loginPageUrl;

    @Value("${gulimail.auth.login-success-redirect:http://gulimail.com}")
    private String loginSuccessRedirect;

    public OAuth2Controller(GithubOAuthApplicationService githubOAuthApplicationService) {
        this.githubOAuthApplicationService = githubOAuthApplicationService;
    }

    @GetMapping("/oauth2.0/github/success")
    public String github(@RequestParam("code") String code,
                         @RequestParam(value = "state", required = false) String state,
                         HttpSession session) {
        OAuthLoginResult result = githubOAuthApplicationService.githubLogin(code, state, session, accessTokenUrl, clientId, clientSecret, redirectUri);
        if (result.isSuccess()) {
            return "redirect:" + loginSuccessRedirect;
        }
        return "redirect:" + loginPageUrl;
    }
}
