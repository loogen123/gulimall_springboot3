package com.lg.common.xss;

import com.lg.common.utils.RRException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SQLFilterTest {

    @Test
    void sqlInjectShouldReturnNullWhenInputBlank() {
        assertNull(SQLFilter.sqlInject(" "));
    }

    @Test
    void sqlInjectShouldSanitizeSafeInput() {
        String value = SQLFilter.sqlInject("create_time");
        assertEquals("create_time", value);
    }

    @Test
    void sqlInjectShouldRejectIllegalKeyword() {
        assertThrows(RRException.class, () -> SQLFilter.sqlInject("name;drop table"));
    }
}
