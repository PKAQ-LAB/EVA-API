package org.pkaq.core.codes;

/**
 * 枚举基类
 *
 * @author PKAQ
 */
public interface BizCode {
    String getMsg();

    String getCode();

    String getPrefix();

    default String getKey() {
        String key = "%s.%s";
        if (this instanceof Enum<?>) {
            return key.formatted(getPrefix(), ((Enum<?>) this).name().toLowerCase());
        }
        return key.formatted(getPrefix(), getCode());
    }
}
