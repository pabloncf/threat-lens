package com.pabloncf.threatlens.detection.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import com.redis.testcontainers.RedisContainer;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Exercises {@link RedisSlidingWindowRateLimiter} against a real Redis container directly
 * (no Spring context) - what matters here is sliding-window behavior under real Redis
 * timing, not the rest of the application.
 */
@Testcontainers
class RedisSlidingWindowRateLimiterTest {

    @Container
    static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    private static LettuceConnectionFactory connectionFactory;
    private static RedisSlidingWindowRateLimiter rateLimiter;

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redis.getRedisHost(), redis.getRedisPort()));
        connectionFactory.afterPropertiesSet();
        StringRedisTemplate redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        rateLimiter = new RedisSlidingWindowRateLimiter(redisTemplate);
    }

    @AfterAll
    static void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void countsOccurrencesWithinTheWindow() {
        // Arrange
        String key = "test:rate-limit:" + UUID.randomUUID();
        Duration window = Duration.ofMinutes(1);

        // Act
        int first = rateLimiter.recordAndCount(key, window);
        int second = rateLimiter.recordAndCount(key, window);
        int third = rateLimiter.recordAndCount(key, window);

        // Assert
        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(2);
        assertThat(third).isEqualTo(3);
    }

    @Test
    void excludesOccurrencesOlderThanTheWindow() {
        // Arrange
        String key = "test:rate-limit:" + UUID.randomUUID();
        Duration tinyWindow = Duration.ofMillis(200);

        // Act
        rateLimiter.recordAndCount(key, tinyWindow);
        await(300);
        int countAfterExpiry = rateLimiter.recordAndCount(key, tinyWindow);

        // Assert
        assertThat(countAfterExpiry).isEqualTo(1);
    }

    @Test
    void tracksDifferentKeysIndependently() {
        // Arrange
        String keyA = "test:rate-limit:" + UUID.randomUUID();
        String keyB = "test:rate-limit:" + UUID.randomUUID();
        Duration window = Duration.ofMinutes(1);

        // Act
        rateLimiter.recordAndCount(keyA, window);
        rateLimiter.recordAndCount(keyA, window);
        int countB = rateLimiter.recordAndCount(keyB, window);

        // Assert
        assertThat(countB).isEqualTo(1);
    }

    private static void await(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
