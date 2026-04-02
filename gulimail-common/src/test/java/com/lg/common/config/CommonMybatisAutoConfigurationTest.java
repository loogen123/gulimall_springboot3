package com.lg.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonMybatisAutoConfigurationTest {
    private final CommonMybatisAutoConfiguration configuration = new CommonMybatisAutoConfiguration();

    @Test
    void shouldCreateMybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = configuration.commonMybatisPlusInterceptor();
        assertNotNull(interceptor);
    }

    @Test
    void shouldCreateMetaObjectHandler() {
        MetaObjectHandler handler = configuration.commonMetaObjectHandler();
        assertNotNull(handler);
    }
}
