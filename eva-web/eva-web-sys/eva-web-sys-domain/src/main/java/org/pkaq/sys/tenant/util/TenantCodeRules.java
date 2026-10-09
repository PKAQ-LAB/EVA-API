package org.pkaq.sys.tenant.util;

import java.util.regex.Pattern;

/**
 * 租户编码是不可复用的 Schema 标识组成部分，不进行大小写或空白归一化。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
public final class TenantCodeRules {
    public static final String CODE_PATTERN = "^[a-z][a-z0-9_]{0,5}$";
    private static final Pattern SAFE_CODE = Pattern.compile(CODE_PATTERN);

    private TenantCodeRules() {
    }

    /**
     * 检查原始编码是否符合小写 ASCII 和六字符上限。
     *
     * @param code 原始租户编码
     * @return 合法返回 true
     */
    public static boolean isValid(String code) {
        return null != code && SAFE_CODE.matcher(code).matches();
    }
}
