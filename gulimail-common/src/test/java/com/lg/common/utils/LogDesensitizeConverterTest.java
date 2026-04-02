package com.lg.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogDesensitizeConverterTest {

    private final LogDesensitizeConverter converter = new LogDesensitizeConverter();

    @Test
    void convertPhone() {
        ILoggingEvent event = createEvent("User registered with phone 13812345678");
        String result = converter.convert(event);
        assertEquals("User registered with phone 138****5678", result);
    }

    @Test
    void convertEmail() {
        ILoggingEvent event = createEvent("Contact email: example@gmail.com");
        String result = converter.convert(event);
        assertEquals("Contact email: e******@gmail.com", result);
    }

    @Test
    void convertMixed() {
        ILoggingEvent event = createEvent("Phone 13988887777 and Email test@outlook.com");
        String result = converter.convert(event);
        assertTrue(result.contains("139****7777"));
        assertTrue(result.contains("t******@outlook.com"));
    }

    private ILoggingEvent createEvent(String message) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return new LoggingEvent(
                Logger.class.getName(),
                context.getLogger(Logger.ROOT_LOGGER_NAME),
                Level.INFO,
                message,
                null,
                null
        );
    }
}
