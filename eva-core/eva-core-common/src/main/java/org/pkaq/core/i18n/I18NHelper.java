package org.pkaq.core.i18n;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.codes.BizCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 国际化工具类
 * @author PKAQ
 */
@Component
@RequiredArgsConstructor
public class I18NHelper {
    private final MessageSource messageSource;

    public String getMsg(BizCode e) {
        return this.getMessage(e.getKey(), e.getMsg());
    }

    /**
     * @param msg ：对应messages配置的key.
     */
    public String getMessage(String msg) {
        if (msg.startsWith("{") && msg.endsWith("}")) {
            // 花括号包裹的
            msg = msg.substring(1, msg.length() - 1);
        }

        return this.getMessage(msg, new Object[]{}, msg);
    }

    /**
     * 获取code值
     */
    public String getMessage(String code, String defaultMessage) {
        return this.getMessage(code, null, defaultMessage);
    }

    /**
     * 获取code值
     */
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return this.getMessage(code, null, defaultMessage, locale);
    }

    /**
     * 获取code值
     */
    public String getMessage(String code, Locale locale) {
        return this.getMessage(code, null, "", locale);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     */
    public String getMessage(String code, Object[] args) {
        return this.getMessage(code, args, "");
    }

    /**
     * 获取code值
     */
    public String getMessage(String key, Object[] args, Locale locale) {
        return this.getMessage(key, args, "", locale);
    }

    /**
     * @param key           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     */
    public String getMessage(String key, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return this.getMessage(key, args, defaultMessage, locale);
    }

    /**
     * 指定语言.
     */
    public String getMessage(String key, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(key, args, defaultMessage, locale);
    }
}
