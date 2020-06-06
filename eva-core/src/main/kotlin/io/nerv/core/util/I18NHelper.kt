package io.nerv.core.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import java.util.*

/**
 * 国际化工具类
 * Author: S.PKAQ
 * Datetime: 2018/3/5 23:55
 */
@Component
class I18NHelper {
    @Autowired
    private val messageSource: MessageSource? = null

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    fun getMessage(code: String?): String {
        return this.getMessage(code, arrayOf())
    }

    /**
     * 获取code值
     * @param code
     * @param defaultMessage
     * @return
     */
    fun getMessage(code: String?, defaultMessage: String?): String {
        return this.getMessage(code, null, defaultMessage)
    }

    /**
     * 获取code值
     * @param code
     * @param defaultMessage
     * @param locale
     * @return
     */
    fun getMessage(code: String?, defaultMessage: String?, locale: Locale?): String {
        return this.getMessage(code, null, defaultMessage, locale)
    }

    /**
     * 获取code值
     * @param code
     * @param locale
     * @return
     */
    fun getMessage(code: String?, locale: Locale?): String {
        return this.getMessage(code, null, "", locale)
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    fun getMessage(code: String?, args: Array<Any?>?): String {
        return this.getMessage(code, args, "")
    }

    /**
     * 获取code值
     * @param code
     * @param args
     * @param locale
     * @return
     */
    fun getMessage(code: String?, args: Array<Any?>?, locale: Locale?): String {
        return this.getMessage(code, args, "", locale)
    }

    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    fun getMessage(code: String?, args: Array<Any?>?, defaultMessage: String?): String {
        //这里使用比较方便的方法，不依赖request.
        val locale = LocaleContextHolder.getLocale()
        return this.getMessage(code, args, defaultMessage, locale)
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
    fun getMessage(code: String?, args: Array<Any?>?, defaultMessage: String?, locale: Locale?): String {
        return messageSource!!.getMessage(code, args, defaultMessage, locale)
    }
}