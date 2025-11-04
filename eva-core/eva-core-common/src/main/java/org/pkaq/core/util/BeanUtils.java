package org.pkaq.core.util;

/**
 * @author spring.utils
 */
public class BeanUtils {
    public static void copyProperties(Object source, Object target, String... ignoreProperties) {
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
