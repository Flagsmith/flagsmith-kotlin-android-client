package com.flagsmith;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.junit.Test;

/** Compiles only while Java callers can still use the 16-parameter constructor. */
public class JavaConstructorCompatibilityTest {
    @Test
    public void javaCallersCanUseTheConstructorWithoutFlushOnBackground() {
        // Given
        FlagsmithCacheConfig cacheConfig = new FlagsmithCacheConfig();

        // When
        Flagsmith flagsmith = new Flagsmith(
                "",
                "http://localhost/",
                "http://localhost/",
                null,
                false,
                false,
                10,
                cacheConfig,
                Collections.emptyList(),
                4L,
                6L,
                6L,
                0.0,
                false,
                "http://localhost/",
                10);

        // Then
        assertNotNull(flagsmith);
        flagsmith.close(0L);
    }
}
