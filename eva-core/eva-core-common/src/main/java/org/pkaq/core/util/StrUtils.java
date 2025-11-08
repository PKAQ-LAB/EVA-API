package org.pkaq.core.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Objects;

/**
 * 字符串工具类
 *
 * @author from apache.common3
 */
public class StrUtils {
    public static boolean isBlank(final CharSequence cs) {
        return StringUtils.isBlank(cs);
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return StringUtils.isNotBlank(cs);
    }

    public static boolean isAllBlank(final CharSequence... css) {
        return StringUtils.isAllBlank(css);
    }

    public static boolean isEmpty(final CharSequence cs) {
        return StringUtils.isEmpty(cs);
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return StringUtils.isNotBlank(cs);
    }

    public static <T> String join(final T... elements) {
        return StringUtils.join(elements);
    }

    public static String removeEnd(final String str, final CharSequence remove) {
        return Strings.CS.removeEnd(str, remove);
    }

    public static String toStringOrNull(Object obj) {
        return Objects.toString(obj, null);
    }

    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return StringUtils.defaultIfBlank(str, defaultStr);
    }

    public static boolean startsWithIgnoreCase(final CharSequence str, final CharSequence prefix) {
        return Strings.CI.startsWith(str, prefix);
    }
}
