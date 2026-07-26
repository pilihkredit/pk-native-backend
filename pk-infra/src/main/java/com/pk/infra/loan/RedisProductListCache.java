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
    public Optional<Long> getProductListId(long userId, String applyId) {
        String value = redisTemplate.opsForValue().get(cacheKey(userId, applyId));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void putProductListId(long userId, String applyId, long productListId) {
        redisTemplate.opsForValue().set(
                cacheKey(userId, applyId),
                Long.toString(productListId),
                cacheTtl
        );
    }

    private static String cacheKey(long userId, String applyId) {
        return KEY_PREFIX + userId + ":" + applyId;
    }
}
