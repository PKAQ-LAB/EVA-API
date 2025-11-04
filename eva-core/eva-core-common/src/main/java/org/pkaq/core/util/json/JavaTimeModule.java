package org.pkaq.core.util.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.pkaq.core.util.DatePatterns;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 日期序列化
 *
 * @author PKAQ
 */
public class JavaTimeModule extends SimpleModule {

    public JavaTimeModule() {
        super(PackageVersion.VERSION);
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DatePatterns.NORM_DATETIME_FORMATTER));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(DatePatterns.NORM_DATE_FORMATTER));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DatePatterns.NORM_TIME_FORMATTER));

        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DatePatterns.NORM_DATETIME_FORMATTER));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(DatePatterns.NORM_DATE_FORMATTER));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(DatePatterns.NORM_TIME_FORMATTER));
    }

}