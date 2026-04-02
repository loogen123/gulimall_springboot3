package com.lg.common;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildEnvironmentBaselineTest {

    @Test
    void javaRuntimeAndTempDirShouldBeAvailable() {
        assertTrue(Runtime.version().feature() >= 17);
        String tempDir = System.getProperty("java.io.tmpdir");
        assertFalse(tempDir == null || tempDir.isBlank());
        assertTrue(Files.exists(Path.of(tempDir)));
    }
}
