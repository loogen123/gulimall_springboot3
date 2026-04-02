package com.lg.gulimail.seckill.controller;

import com.lg.common.utils.R;
import com.lg.gulimail.seckill.service.SeckillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillAdminControllerTest {

    @Mock
    private SeckillService seckillService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private SeckillAdminController seckillAdminController;

    @Test
    void refreshShouldRejectWhenTokenMismatch() {
        ReflectionTestUtils.setField(seckillAdminController, "refreshToken", "token-1");

        R result = seckillAdminController.refresh("bad-token");

        assertEquals(10003, result.getCode());
        verify(seckillService, never()).uploadSeckillSkuLatest3Days();
    }

    @Test
    void refreshShouldRejectWhenTokenNotConfigured() {
        ReflectionTestUtils.setField(seckillAdminController, "refreshToken", "");

        R result = seckillAdminController.refresh("token-1");

        assertEquals(10003, result.getCode());
        verify(seckillService, never()).uploadSeckillSkuLatest3Days();
    }

    @Test
    void refreshShouldClearIndexKeysAndUploadWhenTokenMatch() {
        ReflectionTestUtils.setField(seckillAdminController, "refreshToken", "token-1");
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("seckill:sessions:index")).thenReturn(Set.of("seckill:sessions:1_2"));
        when(setOperations.members("seckill:stock:index")).thenReturn(Set.of("seckill:stock:abc"));

        R result = seckillAdminController.refresh("token-1");

        assertEquals(0, result.getCode());
        verify(redisTemplate).delete(Set.of("seckill:sessions:1_2"));
        verify(redisTemplate).delete("seckill:sessions:index");
        verify(redisTemplate).delete("seckill:skus");
        verify(redisTemplate).delete(Set.of("seckill:stock:abc"));
        verify(redisTemplate).delete("seckill:stock:index");
        verify(seckillService).uploadSeckillSkuLatest3Days();
    }
}
