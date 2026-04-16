package com.visualspider.runtime;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SinaDateTimeParser {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final Pattern MINUTES_AGO = Pattern.compile("^(\\d+)分钟前$");
    private static final Pattern HOURS_AGO = Pattern.compile("^(\\d+)小时前$");

    private final Clock clock;

    public SinaDateTimeParser() {
        this(Clock.system(ZONE_ID));
    }

    SinaDateTimeParser(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();

        for (DateTimeFormatter formatter : new DateTimeFormatter[]{
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
        }) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (normalized.startsWith("今天 ")) {
            return LocalDateTime.parse(now.toLocalDate() + " " + normalized.substring(3), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (normalized.startsWith("昨天 ")) {
            return LocalDateTime.parse(now.toLocalDate().minusDays(1) + " " + normalized.substring(3), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }

        Matcher minutesMatcher = MINUTES_AGO.matcher(normalized);
        if (minutesMatcher.matches()) {
            return now.minusMinutes(Long.parseLong(minutesMatcher.group(1))).withSecond(0).withNano(0);
        }

        Matcher hoursMatcher = HOURS_AGO.matcher(normalized);
        if (hoursMatcher.matches()) {
            return now.minusHours(Long.parseLong(hoursMatcher.group(1))).withSecond(0).withNano(0);
        }

        try {
            return LocalDateTime.parse(LocalDate.now(clock).getYear() + "-" + normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(LocalDate.now(clock).getYear() + "年" + normalized, DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
