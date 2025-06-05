package com.nexacode.template.util;


import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {


    /**
     * @param localDateTime
     * @return
     */
    @Nullable
    public static String getDateTimeString(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    @Nullable
    public static String getDateString(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }


    /**
     * 지정한 타임존으로 localDateTime -> string
     *
     * @param localDateTime
     * @param timezone
     * @return
     */
    @Nullable
    public static String getDateAtTimeZoneString(@Nullable LocalDateTime localDateTime, String timezone) {
        if (localDateTime == null) return null;

        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of(timezone));

        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    /**
     * 지정한 타임존으로 localDate -> string
     *
     * @param localDate
     * @param timezone
     * @return
     */
    @Nullable
    public static String getDateTimeZoneString(@Nullable LocalDate localDate, String timezone) {
        if (localDate == null) return null;

        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of(timezone));

        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * kst localDateTime -> utc localDateTime
     *
     * @param localDateTime
     * @return
     */
    public static LocalDateTime convertToUTC(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        ZonedDateTime localZonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Seoul"));
        ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
        return utcZonedDateTime.toLocalDateTime();
    }

    /**
     * 현재 KST 기준 오늘 날짜 반환
     */
    public static LocalDate getNowKST() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"));
    }


    /**
     * 현재 UTC 기준 오늘 날짜 반환
     */
    public static LocalDate getNowUTC() {
        return LocalDate.now(ZoneId.of("UTC"));
    }
}
