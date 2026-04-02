package com.lg.gulimail.authserver.application.auth;

import lombok.Data;

@Data
public class OAuthLoginResult {
    private boolean success;

    public static OAuthLoginResult success() {
        OAuthLoginResult result = new OAuthLoginResult();
        result.setSuccess(true);
        return result;
    }

    public static OAuthLoginResult failed() {
        OAuthLoginResult result = new OAuthLoginResult();
        result.setSuccess(false);
        return result;
    }
}
