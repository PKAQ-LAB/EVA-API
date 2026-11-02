package org.pkaq.sys.module;

/**
 * 无命名空间的前端操作标识，不参与HTTP权限匹配。
 * @author PKAQ
 * @date 2026-11-01
 */
public final class ModuleActionKey {
    private ModuleActionKey() {
    }

    /** 空值归一为NULL，自定义标识使用最长64字符的lowerCamel形式。 */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String key = value.trim();
        if (!key.matches("[a-z][A-Za-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("操作key格式不合法");
        }
        return key;
    }
}
