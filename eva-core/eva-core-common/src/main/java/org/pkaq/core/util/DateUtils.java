package org.pkaq.core.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {
    public static String format(Date date, String format) {
        return DateFormatUtils.format(date, format);
    }

    public static Date addDay(Date date, int offset) {
        return org.apache.commons.lang3.time.DateUtils.addDays(date, offset);
    }

    public static Date addHours(Date date, int offset) {
        return org.apache.commons.lang3.time.DateUtils.addHours(date, offset);
    }

    public static String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(DatePatterns.NORM_DATETIME_PATTERN));
    }

    public static Date parseDate(String date) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, DatePatterns.NORM_DATE_PATTERN);
    }

    public static Date parseDateTime(String date) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, DatePatterns.NORM_DATETIME_PATTERN);
    }


    public static Date parseDate(String date, String pattern) throws ParseException {
        return org.apache.commons.lang3.time.DateUtils.parseDate(date, pattern);
    }

    public static Date endOfDay(Date date) {
        return org.apache.commons.lang3.time.DateUtils.ceiling(date, java.util.Calendar.DAY_OF_MONTH);
    }

    public static Date beginOfDay(Date date) {
        return org.apache.commons.lang3.time.DateUtils.truncate(date, java.util.Calendar.DAY_OF_MONTH);
    }

}
