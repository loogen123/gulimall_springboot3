package com.lg.common.utils;

import com.lg.common.exception.BizCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RRExceptionTest {

    @Test
    void testConstructorWithBizCode() {
        RRException exception = new RRException(BizCodeEnum.VAILD_EXCEPTION);
        assertEquals(BizCodeEnum.VAILD_EXCEPTION.getCode(), exception.getCode());
        assertEquals(BizCodeEnum.VAILD_EXCEPTION.getMsg(), exception.getMsg());
    }

    @Test
    void testConstructorWithBizCodeAndThrowable() {
        RuntimeException cause = new RuntimeException("cause");
        RRException exception = new RRException(BizCodeEnum.FORBIDDEN_EXCEPTION, cause);
        assertEquals(BizCodeEnum.FORBIDDEN_EXCEPTION.getCode(), exception.getCode());
        assertEquals(BizCodeEnum.FORBIDDEN_EXCEPTION.getMsg(), exception.getMsg());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDefaultConstructor() {
        RRException exception = new RRException("error message", 401);
        assertEquals(401, exception.getCode());
        assertEquals("error message", exception.getMsg());
    }
}
