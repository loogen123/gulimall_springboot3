package com.lg.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaRuntimeBaselineTest {

    @Test
    void runtimeShouldBeJava17OrHigher() {
        assertTrue(Runtime.version().feature() >= 17);
    }
}
