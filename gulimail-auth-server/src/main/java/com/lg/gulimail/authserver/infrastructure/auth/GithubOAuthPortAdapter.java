package com.lg.gulimail.authserver.infrastructure.auth;

import com.lg.gulimail.authserver.application.port.out.GithubOAuthPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
public class GithubOAuthPortAdapter implements GithubOAuthPort {
    private final RestTemplate restTemplate;

    public GithubOAuthPortAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String requestAccessToken(String accessTokenUrl, String clientId, String clientSecret, String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", redirectUri);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(accessTokenUrl, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return null;
        }
        return response.getBody();
    }
}
