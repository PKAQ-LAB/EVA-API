package org.pkaq.core.enums;

public interface BaseEnum<T> {
    T getCode();

    static <E extends Enum<E> & BaseEnum<T>, T> E of(Class<E> enumClass, Object code) {
        if (code == null) return null;
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().toString().equals(code.toString())) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code + " for enum " + enumClass.getSimpleName());
    }
}
