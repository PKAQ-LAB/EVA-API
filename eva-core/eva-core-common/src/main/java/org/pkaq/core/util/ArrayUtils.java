package org.pkaq.core.util;

/**
 * @author apache.common-lang3
 */
public class ArrayUtils {
    public static boolean contains(final Object[] array, final Object objectToFind) {
        return org.apache.commons.lang3.ArrayUtils.contains(array, objectToFind);
    }

    public static boolean isEmpty(final Object[] array) {
        return org.apache.commons.lang3.ArrayUtils.isEmpty(array);
    }

    public static <T> boolean isNotEmpty(final T[] array) {
        return !isEmpty(array);
    }

    public static <T> T[] add(final T[] array, final T element) {
        return org.apache.commons.lang3.ArrayUtils.add(array, element);
    }

    public static <T> T[] addAll(final T[] array1, @SuppressWarnings("unchecked") final T... array2) {
        return org.apache.commons.lang3.ArrayUtils.addAll(array1, array2);
    }
}
