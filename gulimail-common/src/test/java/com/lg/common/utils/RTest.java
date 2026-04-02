package com.lg.common.utils;

import com.lg.common.exception.BizCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RTest {

    @Test
    void testOkWithBizCode() {
        R r = R.ok(BizCodeEnum.VAILD_EXCEPTION);
        assertEquals(BizCodeEnum.VAILD_EXCEPTION.getCode(), r.get("code"));
        assertEquals(BizCodeEnum.VAILD_EXCEPTION.getMsg(), r.get("msg"));
    }

    @Test
    void testErrorWithBizCode() {
        R r = R.error(BizCodeEnum.TOO_MANY_REQUESTS);
        assertEquals(BizCodeEnum.TOO_MANY_REQUESTS.getCode(), r.get("code"));
        assertEquals(BizCodeEnum.TOO_MANY_REQUESTS.getMsg(), r.get("msg"));
    }

    @Test
    void testDefaultOk() {
        R r = R.ok();
        assertEquals(0, r.get("code"));
        assertEquals("success", r.get("msg"));
    }

    @Test
    void testDefaultError() {
        R r = R.error();
        assertEquals(500, r.get("code"));
        assertNotNull(r.get("msg"));
    }
}
