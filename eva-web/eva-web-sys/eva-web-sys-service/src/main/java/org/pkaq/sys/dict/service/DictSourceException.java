package org.pkaq.sys.dict.service;

/**
 * 已确认的字典来源或结构冲突；同步端点单独转换为既有业务失败格式。
 * @author PKAQ
 * @date 2026-11-01
 */
public final class DictSourceException extends IllegalStateException {
    /** 创建不包含实际来源信息的异常。 */
    public DictSourceException() {
        super("字典快照来源或结构不可用");
    }
}
