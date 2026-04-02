package com.lg.gulimail.authserver.application.auth;

import com.lg.common.constant.AuthServerConstant;
import com.lg.common.utils.R;
import com.lg.common.vo.UserLoginVo;
import com.lg.gulimail.authserver.application.port.out.AuthMemberPort;
import com.lg.gulimail.authserver.application.port.out.AuthThirdPartyPort;
import com.lg.gulimail.authserver.domain.auth.AuthDomainService;
import com.lg.gulimail.authserver.vo.UserRegisterVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginApplicationServiceTest {
    @Mock
    private AuthMemberPort authMemberPort;
    @Mock
    private AuthThirdPartyPort authThirdPartyPort;
    @Mock
    private AuthDomainService authDomainService;
    @InjectMocks
    private LoginApplicationService loginApplicationService;

    @Test
    void sendCodeShouldRejectInvalidPhone() {
        when(authDomainService.normalizePhone("123")).thenReturn("123");
        when(authDomainService.isValidPhone("123")).thenReturn(false);

        R result = loginApplicationService.sendCode("123");

        assertEquals(10001, result.getCode());
        verifyNoInteractions(authThirdPartyPort);
    }

    @Test
    void loginShouldSetSessionWhenSuccess() {
        UserLoginVo vo = new UserLoginVo();
        BindingResult br = new BeanPropertyBindingResult(vo, "vo");
        when(authDomainService.toErrorMap(br)).thenReturn(java.util.Map.of());
        when(authMemberPort.login(vo)).thenReturn(R.ok().put("member", new com.lg.common.vo.MemberResponseVo()));
        MockHttpSession session = new MockHttpSession();

        R result = loginApplicationService.login(vo, br, session);

        assertEquals(0, result.getCode());
        assertNotNull(session.getAttribute(AuthServerConstant.LOGIN_USER));
    }

    @Test
    void registerShouldRejectWhenCodeInvalid() {
        UserRegisterVo vo = new UserRegisterVo();
        vo.setPhone("13800138000");
        vo.setCode("123456");
        BindingResult br = new BeanPropertyBindingResult(vo, "vo");
        when(authDomainService.toErrorMap(br)).thenReturn(java.util.Map.of());
        when(authThirdPartyPort.checkCode("13800138000", "123456")).thenReturn(R.error("bad"));

        R result = loginApplicationService.register(vo, br);

        assertEquals(10001, result.getCode());
        verify(authThirdPartyPort).checkCode("13800138000", "123456");
    }
}
