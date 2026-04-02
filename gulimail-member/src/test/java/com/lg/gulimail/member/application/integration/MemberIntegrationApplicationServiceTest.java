package com.lg.gulimail.member.application.integration;

import com.lg.gulimail.member.application.port.out.MemberIntegrationPort;
import com.lg.gulimail.member.domain.integration.MemberIntegrationDomainService;
import com.lg.gulimail.member.domain.integration.MemberIntegrationMutationResult;
import com.lg.gulimail.member.domain.integration.MemberIntegrationQuoteResult;
import com.lg.gulimail.member.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberIntegrationApplicationServiceTest {
    @Mock
    private MemberIntegrationPort memberIntegrationPort;

    private MemberIntegrationApplicationService memberIntegrationApplicationService;

    @BeforeEach
    void setUp() {
        memberIntegrationApplicationService =
                new MemberIntegrationApplicationService(memberIntegrationPort, new MemberIntegrationDomainService());
    }

    @Test
    void quoteShouldReturnNotFoundWhenMemberMissing() {
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", 1L);
        request.put("useIntegration", 10);
        request.put("orderTotal", new BigDecimal("9.90"));
        when(memberIntegrationPort.findMemberById(1L)).thenReturn(null);

        MemberIntegrationQuoteResult result = memberIntegrationApplicationService.quote(request);

        assertEquals(10005, result.getCode());
    }

    @Test
    void quoteShouldReturnUseIntegrationAndAmount() {
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", 1L);
        request.put("useIntegration", 300);
        request.put("orderTotal", new BigDecimal("9.90"));
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(1L);
        memberEntity.setIntegration(1000);
        when(memberIntegrationPort.findMemberById(1L)).thenReturn(memberEntity);

        MemberIntegrationQuoteResult result = memberIntegrationApplicationService.quote(request);

        assertTrue(result.isSuccess());
        assertEquals(300, result.getUseIntegration());
        assertEquals(new BigDecimal("3.00"), result.getIntegrationAmount());
    }

    @Test
    void deductShouldReturnValidationErrorWhenRequestInvalid() {
        MemberIntegrationMutationResult result = memberIntegrationApplicationService.deduct(new HashMap<>());
        assertEquals(10001, result.getCode());
    }

    @Test
    void revertShouldSkipWhenDeductHistoryMissing() {
        Map<String, Object> request = new HashMap<>();
        request.put("memberId", 1L);
        request.put("useIntegration", 100);
        request.put("orderSn", "order-1");
        when(memberIntegrationPort.findHistoryByMemberIdAndNote(1L, "ORDER_REVERT:order-1")).thenReturn(null);
        when(memberIntegrationPort.findHistoryByMemberIdAndNote(1L, "ORDER_DEDUCT:order-1")).thenReturn(null);

        MemberIntegrationMutationResult result = memberIntegrationApplicationService.revert(request);

        assertTrue(result.isSuccess());
        verify(memberIntegrationPort).findHistoryByMemberIdAndNote(1L, "ORDER_DEDUCT:order-1");
    }
}
