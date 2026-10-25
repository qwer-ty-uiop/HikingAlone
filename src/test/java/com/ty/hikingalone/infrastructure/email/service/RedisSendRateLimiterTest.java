package com.ty.hikingalone.infrastructure.email.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * IP 限流器单测：Mockito 模拟 RedisTemplate，验证配额判定与 fail-open 契约
 */
class RedisSendRateLimiterTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);

    private RedisSendRateLimiter limiter(int ipLimit) {
        RedisSendRateLimiter limiter = new RedisSendRateLimiter(redisTemplate);
        ReflectionTestUtils.setField(limiter, "ipLimit", ipLimit);
        ReflectionTestUtils.setField(limiter, "windowSeconds", 3600);
        return limiter;
    }

    @Test
    void withinQuotaAllowedAndBeyondDenied() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(1L, 2L, 10L, 11L);

        RedisSendRateLimiter limiter = limiter(10);

        assertThat(limiter.tryAcquire("1.1.1.1")).as("第 1 次应放行").isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).as("第 2 次应放行").isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).as("第 10 次应放行").isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1")).as("第 11 次应拒绝").isFalse();
    }

    @Test
    void redisFailureFailsOpen() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString()))
                .thenThrow(new RedisConnectionFailureException("connection refused"));

        assertThat(limiter(10).tryAcquire("1.1.1.1"))
                .as("Redis 故障应放行（fail-open，单邮箱冷却兜底），不得把用户挡成 500")
                .isTrue();
    }
}
