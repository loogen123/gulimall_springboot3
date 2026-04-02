package com.lg.common.exception;

import com.lg.common.utils.R;
import com.lg.common.utils.RRException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GulimailExceptionControllerAdviceTest {

    private final GulimailExceptionControllerAdvice advice = new GulimailExceptionControllerAdvice();

    @Test
    void shouldReturnBusinessCodeForRRException() {
        RRException ex = new RRException(BizCodeEnum.UNAUTHORIZED_EXCEPTION.getMsg(), BizCodeEnum.UNAUTHORIZED_EXCEPTION.getCode());
        R result = advice.handleRRException(ex);
        assertEquals(BizCodeEnum.UNAUTHORIZED_EXCEPTION.getCode(), result.get("code"));
        assertEquals(BizCodeEnum.UNAUTHORIZED_EXCEPTION.getMsg(), result.get("msg"));
    }
}

