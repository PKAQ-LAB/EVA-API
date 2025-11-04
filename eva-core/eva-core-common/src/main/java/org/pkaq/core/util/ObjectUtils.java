package org.pkaq.core.util;

public class ObjectUtils {
    public static boolean isNull(final Object object) {
        return null == object || object.equals(null);
    }
}
