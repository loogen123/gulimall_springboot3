package com.lg.common.xss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HTMLFilterTest {

    @Test
    void filterShouldRemoveScriptTag() {
        HTMLFilter htmlFilter = new HTMLFilter();

        String result = htmlFilter.filter("<script>alert(1)</script><b>ok</b>");

        assertFalse(result.contains("<script"));
        assertTrue(result.contains("<b>ok</b>"));
    }

    @Test
    void filterShouldKeepAllowedHrefProtocol() {
        HTMLFilter htmlFilter = new HTMLFilter();

        String result = htmlFilter.filter("<a href=\"https://gulimail.com\">go</a>");

        assertTrue(result.contains("href=\"https://gulimail.com\""));
        assertTrue(result.contains(">go</a>"));
    }
}
