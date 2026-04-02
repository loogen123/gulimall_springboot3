package com.lg.gulimail.order.controller;

import com.lg.common.utils.PageUtils;
import com.lg.common.utils.R;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.order.interceptor.LoginUserInterceptor;
import com.lg.gulimail.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @AfterEach
    void clearThreadLocal() {
        LoginUserInterceptor.loginUser.remove();
    }

    @Test
    void listWithItemShouldReturnUnauthorizedWhenNoLoginUser() {
        Map<String, Object> params = new HashMap<>();
        R result = orderController.listWithItem(params);
        assertEquals(401, result.getCode());
        verify(orderService, never()).queryPageWithItem(params);
    }

    @Test
    void listWithItemShouldReturnPageWhenLoginUserExists() {
        MemberResponseVo member = new MemberResponseVo();
        member.setId(1L);
        LoginUserInterceptor.loginUser.set(member);
        Map<String, Object> params = new HashMap<>();
        PageUtils pageUtils = mock(PageUtils.class);
        when(orderService.queryPageWithItem(params)).thenReturn(pageUtils);

        R result = orderController.listWithItem(params);

        assertEquals(0, result.getCode());
        assertNotNull(result.get("page"));
        assertSame(pageUtils, result.get("page"));
        verify(orderService).queryPageWithItem(params);
    }
}
