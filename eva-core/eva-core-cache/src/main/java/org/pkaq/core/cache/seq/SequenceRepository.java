package org.pkaq.core.cache.seq;

import org.pkaq.core.cache.CacheCodes;
import org.pkaq.core.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * @author PKAQ
 */
@Repository
public class SequenceRepository {

    private final StringRedisTemplate redisTemplate;

    public SequenceRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long getNextSequence(String tenantId, String date, String resetPeriod) {
        String redisKey = buildRedisKey(tenantId, date);
        long seqNum = redisTemplate.opsForValue().increment(redisKey);
        if (seqNum == 1) {
            resetSequenceExpiration(redisKey, resetPeriod);
        }
        return seqNum;
    }

    private String buildRedisKey(String tenantId, String date) {
        return String.format("seq:%s:%s", tenantId, date);
    }

    private void resetSequenceExpiration(String redisKey, String resetPeriod) {
        long duration;
        TimeUnit timeUnit = switch (resetPeriod) {
            case "D" -> {
                duration = 1;
                yield TimeUnit.DAYS;
            }
            case "M" -> {
                duration = LocalDate.now().lengthOfMonth();
                yield TimeUnit.DAYS;
            }
            case "Y" -> {
                duration = 365;
                yield TimeUnit.DAYS;
            }
            default -> throw new BizException(CacheCodes.SERVER_ERROR_CACHE_PERIOD, "Unsupported reset period: " + resetPeriod);
        };
        redisTemplate.expire(redisKey, duration, timeUnit);
    }
}
