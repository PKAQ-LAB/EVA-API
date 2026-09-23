package org.pkaq.core.enums;

import org.pkaq.core.codes.CommonCodes;
import org.pkaq.core.exception.BizException;

/**
 * 自定义美剧映射
 *
 * @author PKAQ
 */
public interface BaseEnum<T> {
    static <E extends Enum<E> & BaseEnum<T>, T> E of(Class<E> enumClass, Object code) {
        if (code == null) return null;
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().toString().equals(code.toString())) {
                return e;
            }
        }
        throw new BizException(CommonCodes.SERVER_ERROR_ENUM_UNKNOWN_CODE);
    }

    T getCode();
}
