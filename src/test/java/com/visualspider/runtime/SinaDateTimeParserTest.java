package com.visualspider.runtime;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SinaDateTimeParserTest {

    private final SinaDateTimeParser parser = new SinaDateTimeParser(
            Clock.fixed(Instant.parse("2026-04-16T02:30:00Z"), ZoneId.of("Asia/Shanghai"))
    );

    @Test
    void shouldParseChineseDateTime() {
        assertEquals(
                LocalDateTime.of(2026, 4, 16, 9, 30),
                parser.parse("2026年04月16日 09:30")
        );
    }

    @Test
    void shouldParseRelativeMinutesAgo() {
        assertEquals(
                LocalDateTime.of(2026, 4, 16, 10, 25),
                parser.parse("5分钟前")
        );
    }
}
