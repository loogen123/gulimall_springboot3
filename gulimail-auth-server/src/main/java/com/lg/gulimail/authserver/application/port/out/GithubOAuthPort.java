package com.lg.gulimail.authserver.application.port.out;

public interface GithubOAuthPort {
    String requestAccessToken(String accessTokenUrl, String clientId, String clientSecret, String code, String redirectUri);
}
