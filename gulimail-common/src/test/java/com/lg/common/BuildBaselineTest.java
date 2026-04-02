package com.lg.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildBaselineTest {

    @Test
    void baselineSystemPropertiesShouldBeAvailable() {
        String userDir = System.getProperty("user.dir");
        String fileSeparator = System.getProperty("file.separator");

        assertFalse(userDir == null || userDir.isBlank());
        assertTrue("/".equals(fileSeparator) || "\\".equals(fileSeparator));
    }
}
