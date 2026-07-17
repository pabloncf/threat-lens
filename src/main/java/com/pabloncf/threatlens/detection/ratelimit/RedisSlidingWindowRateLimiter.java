package com.pabloncf.threatlens.detection.ratelimit;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Sliding-window-log rate limiter backed by a Redis sorted set: each occurrence is added with
 * its timestamp as the score, entries older than the window are evicted, and the remaining
 * count is the sorted set's cardinality. More precise than a fixed-window counter since it
 * doesn't reset abruptly at window boundaries.
 */
@Component
public class RedisSlidingWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RedisSlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public int recordAndCount(String key, Duration window) {
        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();
        String member = now + "-" + UUID.randomUUID();

        redisTemplate.opsForZSet().add(key, member, now);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        redisTemplate.expire(key, window);

        Long count = redisTemplate.opsForZSet().zCard(key);
        return count == null ? 0 : count.intValue();
    }
}
