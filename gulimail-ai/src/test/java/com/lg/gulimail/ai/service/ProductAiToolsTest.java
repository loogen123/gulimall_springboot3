package com.lg.gulimail.ai.service;

import com.lg.common.utils.R;
import com.lg.gulimail.ai.feign.ProductFeignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ProductAiToolsTest {

    @Mock
    private ProductFeignService productFeignService;

    @InjectMocks
    private ProductAiTools productAiTools;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSkuDetails() {
        // Prepare mock response
        Map<String, Object> data = new HashMap<>();
        data.put("skuName", "华为 Mate 60 Pro");
        R r = R.ok().put("data", data);
        
        when(productFeignService.getSkuItem(anyLong())).thenReturn(r);

        // Execute
        String result = productAiTools.getSkuDetails(1L);

        // Verify
        assertTrue(result.contains("华为 Mate 60 Pro"));
    }
}
