package org.pkaq.core.idempotency;

import java.time.Duration;

/**
 * 原子占用防重复提交键的中立存储契约。
 *
 * @author PKAQ
 * @date 2026-10-09
 */
public interface IIdempotencyStore {
    /**
     * 在指定窗口内原子占用键，不延长已存在键的有效期。
     *
     * @param key 业务防重键
     * @param ttl 占用有效期
     * @return 首次成功占用返回 true，已占用或未成功写入返回 false
     */
    boolean acquire(String key, Duration ttl);
}
