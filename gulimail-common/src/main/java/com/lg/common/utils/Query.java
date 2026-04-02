/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.lg.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lg.common.xss.SQLFilter;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 查询参数
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Query<T> {
    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_LIMIT = 10L;
    private static final long MAX_LIMIT = 200L;
    private static final String DESC = "desc";

    public IPage<T> getPage(Map<String, Object> params) {
        return this.getPage(params, null, false);
    }

    public IPage<T> getPage(Map<String, Object> params, String defaultOrderField, boolean isAsc) {
        long curPage = parsePositiveLong(params.get(Constant.PAGE), DEFAULT_PAGE);
        long limit = parsePositiveLong(params.get(Constant.LIMIT), DEFAULT_LIMIT);
        limit = Math.min(limit, MAX_LIMIT);

        Page<T> page = new Page<>(curPage, limit);

        params.put(Constant.PAGE, page);

        String orderField = SQLFilter.sqlInject(toStringValue(params.get(Constant.ORDER_FIELD)));
        String order = toStringValue(params.get(Constant.ORDER));


        if(StringUtils.isNotEmpty(orderField) && isSupportedOrder(order)){
            if(Constant.ASC.equalsIgnoreCase(order)) {
                return  page.addOrder(OrderItem.asc(orderField));
            }else if (DESC.equalsIgnoreCase(order)) {
                return page.addOrder(OrderItem.desc(orderField));
            }
        }

        if(StringUtils.isBlank(defaultOrderField)){
            return page;
        }

        if(isAsc) {
            page.addOrder(OrderItem.asc(defaultOrderField));
        }else {
            page.addOrder(OrderItem.desc(defaultOrderField));
        }

        return page;
    }

    private long parsePositiveLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value);
        if (StringUtils.isBlank(text)) {
            return defaultValue;
        }
        try {
            long parsed = Long.parseLong(text.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private boolean isSupportedOrder(String order) {
        return StringUtils.isNotEmpty(order)
                && (Constant.ASC.equalsIgnoreCase(order) || DESC.equalsIgnoreCase(order));
    }
}
