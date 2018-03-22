package org.pkaq.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类
 * Author: S.PKAQ
 * Datetime: 2018/3/5 23:55
 */
@Component
public class I18NHelper {
    @Autowired
    private MessageSource messageSource;

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    public String getMessage(String code) {
        return this.getMessage(code, new Object[]{});
    }

    /**
     * 获取code值
     * @param code
     * @param defaultMessage
     * @return
     */
    public String getMessage(String code, String defaultMessage) {
        return this.getMessage(code, null, defaultMessage);
    }

    /**
     * 获取code值
     * @param code
     * @param defaultMessage
     * @param locale
     * @return
     */
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return this.getMessage(code, null, defaultMessage, locale);
    }

    /**
     * 获取code值
     * @param code
     * @param locale
     * @return
     */
    public String getMessage(String code, Locale locale) {
        return this.getMessage(code, null, "", locale);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    public String getMessage(String code, Object[] args) {
        return this.getMessage(code, args, "");
    }

    /**
     * 获取code值
     * @param code
     * @param args
     * @param locale
     * @return
     */
    public String getMessage(String code, Object[] args, Locale locale) {
        return this.getMessage(code, args, "", locale);
    }
    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return this.getMessage(code, args, defaultMessage, locale);
    }
    /**
     * 指定语言.
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}
