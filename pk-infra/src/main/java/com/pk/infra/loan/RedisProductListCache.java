package com.pk.infra.loan;

import com.pk.core.loan.port.ProductListCache;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisProductListCache implements ProductListCache {
    private static final String KEY_PREFIX = "pk:loan:products:";

    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;

    public RedisProductListCache(StringRedisTemplate redisTemplate, LoanProductProperties loanProductProperties) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = loanProductProperties.cacheTtl();
    }

    @Override
    public Optional<String> getSnapshotNo(long profileId, String applyId) {
        String value = redisTemplate.opsForValue().get(cacheKey(profileId, applyId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    @Override
    public void putSnapshotNo(long profileId, String applyId, String snapshotNo) {
        redisTemplate.opsForValue().set(cacheKey(profileId, applyId), snapshotNo, cacheTtl);
    }

    private static String cacheKey(long profileId, String applyId) {
        return KEY_PREFIX + profileId + ":" + applyId;
    }
}
