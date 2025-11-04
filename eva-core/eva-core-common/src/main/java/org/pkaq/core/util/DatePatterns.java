package org.pkaq.core.util;

import java.time.format.DateTimeFormatter;

public interface DatePatterns {
    String NORM_DATE_PATTERN = "yyyy-MM-dd";
    String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    String NORM_TIME_PATTERN = "HH:mm:ss";

    DateTimeFormatter NORM_DATE_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATE_PATTERN);
    DateTimeFormatter NORM_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_DATETIME_PATTERN);
    DateTimeFormatter NORM_TIME_FORMATTER = DateTimeFormatter.ofPattern(NORM_TIME_PATTERN);
}
