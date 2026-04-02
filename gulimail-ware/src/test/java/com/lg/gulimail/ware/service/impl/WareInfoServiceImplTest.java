package com.lg.gulimail.ware.service.impl;

import com.lg.common.utils.R;
import com.lg.common.vo.FareVo;
import com.lg.common.vo.MemberAddressVo;
import com.lg.gulimail.ware.feign.MemberFeignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WareInfoServiceImplTest {

    @Mock
    private MemberFeignService memberFeignService;

    @InjectMocks
    private WareInfoServiceImpl wareInfoService;

    @Test
    void getFareShouldReturnZeroWhenPhoneBlank() {
        MemberAddressVo addressVo = new MemberAddressVo();
        addressVo.setPhone("");
        when(memberFeignService.info(1L)).thenReturn(R.ok().put("memberReceiveAddress", addressVo));

        FareVo fareVo = wareInfoService.getFare(1L);

        assertNotNull(fareVo);
        assertEquals(BigDecimal.ZERO, fareVo.getFare());
    }

    @Test
    void getFareShouldReturnNullWhenAddressMissing() {
        when(memberFeignService.info(2L)).thenReturn(R.ok());

        FareVo fareVo = wareInfoService.getFare(2L);

        assertNull(fareVo);
    }
}
