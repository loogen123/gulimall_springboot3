package com.lg.gulimail.member.controller;

import com.lg.common.utils.RRException;
import com.lg.common.vo.MemberResponseVo;
import com.lg.gulimail.member.entity.MemberReceiveAddressEntity;
import com.lg.gulimail.member.interceptor.LoginUserInterceptor;
import com.lg.gulimail.member.service.MemberReceiveAddressService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberReceiveAddressControllerSecurityTest {

    private final MemberReceiveAddressService memberReceiveAddressService = mock(MemberReceiveAddressService.class);
    private final MemberReceiveAddressController controller = new MemberReceiveAddressController();

    MemberReceiveAddressControllerSecurityTest() {
        ReflectionTestUtils.setField(controller, "memberReceiveAddressService", memberReceiveAddressService);
    }

    @AfterEach
    void cleanup() {
        LoginUserInterceptor.loginUser.remove();
    }

    @Test
    void shouldRejectWhenUserNotLogin() {
        RRException ex = assertThrows(RRException.class, () -> controller.getAddress(1L));
        assertEquals(10002, ex.getCode());
    }

    @Test
    void shouldRejectWhenMemberIdMismatch() {
        MemberResponseVo loginUser = new MemberResponseVo();
        loginUser.setId(2L);
        LoginUserInterceptor.loginUser.set(loginUser);
        RRException ex = assertThrows(RRException.class, () -> controller.getAddress(1L));
        assertEquals(10003, ex.getCode());
    }

    @Test
    void shouldOnlyQueryCurrentLoginUserAddress() {
        MemberResponseVo loginUser = new MemberResponseVo();
        loginUser.setId(2L);
        LoginUserInterceptor.loginUser.set(loginUser);
        when(memberReceiveAddressService.getAddress(2L)).thenReturn(Collections.emptyList());
        List<MemberReceiveAddressEntity> result = controller.getAddress(2L);
        assertEquals(0, result.size());
    }

    @Test
    void deleteShouldRejectWhenIdsEmpty() {
        MemberResponseVo loginUser = new MemberResponseVo();
        loginUser.setId(2L);
        LoginUserInterceptor.loginUser.set(loginUser);

        RRException ex = assertThrows(RRException.class, () -> controller.delete(new Long[0]));

        assertEquals(10001, ex.getCode());
    }

    @Test
    void deleteShouldBatchValidateOwnership() {
        MemberResponseVo loginUser = new MemberResponseVo();
        loginUser.setId(2L);
        LoginUserInterceptor.loginUser.set(loginUser);
        MemberReceiveAddressEntity address = new MemberReceiveAddressEntity();
        address.setId(10L);
        address.setMemberId(2L);
        when(memberReceiveAddressService.listByIds(List.of(10L))).thenReturn(List.of(address));

        controller.delete(new Long[]{10L});

        verify(memberReceiveAddressService).removeByIds(List.of(10L));
    }

    @Test
    void listShouldInjectCurrentMemberIdForQuery() {
        MemberResponseVo loginUser = new MemberResponseVo();
        loginUser.setId(9L);
        LoginUserInterceptor.loginUser.set(loginUser);
        when(memberReceiveAddressService.queryPage(argThat(map -> map != null && Long.valueOf(9L).equals(map.get("member_id")))))
                .thenReturn(null);

        controller.list(new HashMap<>());

        verify(memberReceiveAddressService).queryPage(argThat(map -> map != null && Long.valueOf(9L).equals(map.get("member_id"))));
    }
}
