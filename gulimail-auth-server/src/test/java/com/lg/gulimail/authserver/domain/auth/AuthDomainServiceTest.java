package com.lg.gulimail.authserver.domain.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthDomainServiceTest {
    private final AuthDomainService authDomainService = new AuthDomainService();

    @Test
    void generateStateShouldReturnNonEmpty() {
        String state = authDomainService.generateState();
        assertNotNull(state);
        assertTrue(state.length() >= 16);
    }

    @Test
    void normalizePhoneShouldTrim() {
        assertEquals("13800138000", authDomainService.normalizePhone(" 13800138000 "));
    }

    @Test
    void extractAccessTokenShouldTrimToken() {
        String token = authDomainService.extractAccessToken("{\"access_token\":\" token-1 \"}");
        assertEquals("token-1", token);
    }

    @Test
    void extractAccessTokenShouldReturnNullWhenBodyInvalid() {
        assertNull(authDomainService.extractAccessToken("{}"));
    }
}
