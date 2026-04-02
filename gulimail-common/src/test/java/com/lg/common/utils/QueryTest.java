package com.lg.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryTest {

    private final Query<Object> query = new Query<>();

    @Test
    void getPageShouldUseDefaultValuesWhenInputInvalid() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, "abc");
        params.put(Constant.LIMIT, "-1");

        IPage<Object> page = query.getPage(params);

        assertEquals(1L, page.getCurrent());
        assertEquals(10L, page.getSize());
        assertNotNull(params.get(Constant.PAGE));
    }

    @Test
    void getPageShouldClampLimitWhenTooLarge() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, "2");
        params.put(Constant.LIMIT, "9999");

        IPage<Object> page = query.getPage(params);

        assertEquals(2L, page.getCurrent());
        assertEquals(200L, page.getSize());
    }

    @Test
    void getPageShouldSupportNumericInputAndOrderByAsc() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, 3);
        params.put(Constant.LIMIT, 20);
        params.put(Constant.ORDER_FIELD, "id");
        params.put(Constant.ORDER, "asc");

        IPage<Object> page = query.getPage(params);

        assertEquals(3L, page.getCurrent());
        assertEquals(20L, page.getSize());
        List<OrderItem> orders = page.orders();
        assertEquals(1, orders.size());
        assertEquals("id", orders.get(0).getColumn());
        assertTrue(orders.get(0).isAsc());
    }

    @Test
    void getPageShouldIgnoreOrderWhenOrderTypeUnsupported() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, "1");
        params.put(Constant.LIMIT, "10");
        params.put(Constant.ORDER_FIELD, "id");
        params.put(Constant.ORDER, "drop");

        IPage<Object> page = query.getPage(params);

        assertEquals(0, page.orders().size());
    }

    @Test
    void getPageShouldTrimNumericAndOrderTextValues() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, " 2 ");
        params.put(Constant.LIMIT, " 15 ");
        params.put(Constant.ORDER_FIELD, " id ");
        params.put(Constant.ORDER, " DESC ");

        IPage<Object> page = query.getPage(params);

        assertEquals(2L, page.getCurrent());
        assertEquals(15L, page.getSize());
        assertEquals(1, page.orders().size());
        assertEquals("id", page.orders().get(0).getColumn());
        assertTrue(!page.orders().get(0).isAsc());
    }
}
