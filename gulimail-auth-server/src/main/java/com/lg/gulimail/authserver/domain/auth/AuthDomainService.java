package com.lg.gulimail.authserver.domain.auth;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthDomainService {
    private static final String PHONE_REGEX = "^1\\d{10}$";

    public String generateState() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.trim();
    }

    public boolean isValidPhone(String phone) {
        return StringUtils.hasText(phone) && phone.matches(PHONE_REGEX);
    }

    public Map<String, String> toErrorMap(BindingResult result) {
        if (result == null || !result.hasErrors()) {
            return new HashMap<>();
        }
        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (v1, v2) -> v1));
    }

    public String extractAccessToken(String body) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(body);
        String accessToken = jsonObject.getString("access_token");
        if (!StringUtils.hasText(accessToken)) {
            return null;
        }
        return accessToken.trim();
    }
}
