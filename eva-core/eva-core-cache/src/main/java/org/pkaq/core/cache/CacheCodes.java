package org.pkaq.core.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pkaq.core.exception.BizAssert;

/**
 * 状态码
 * 错误统一为 4xxx
 *
 * @author PKAQ
 */
@Getter
@AllArgsConstructor
public enum CacheCodes implements BizAssert {
    //    Unsupported reset period
    SERVER_ERROR_CACHE_PERIOD("服务器发生错误,请联系管理员", "0x000-07000");

    /**
     * 名称
     */
    private final String msg;
    /**
     * 索引
     */
    private final String code;

    private final String prefix = "common";
}
