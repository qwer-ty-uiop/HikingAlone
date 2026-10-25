package com.ty.hikingalone.infrastructure.email.service;

import com.ty.hikingalone.domain.email.service.SendRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 发送频控的 Redis 计数窗口实现（基础设施层）：
 * key {@code email:ip:quota:<ip>} 计数，首次计数时设置窗口 TTL——固定窗口够用
 * （防刷不追求平滑），窗口边界双倍流量的经典问题在本量级可接受。
 * <p>IP 取值见 EmailController：单可信代理（nginx）拓扑下取 X-Forwarded-For 的
 * <b>最后一个</b>元素（nginx 追加的真实客户端地址，不可伪造）。</p>
 */
@Repository
@RequiredArgsConstructor
public class RedisSendRateLimiter implements SendRateLimiter {

    private static final String KEY_PREFIX = "email:ip:quota:";

    private final RedisTemplate<String, String> redisTemplate;

    /** 窗口内单 IP 最大发送次数，application.yml register.code.ip-limit */
    @Value("${register.code.ip-limit:10}")
    private int ipLimit;

    /** 计数窗口（秒），application.yml register.code.ip-window-seconds */
    @Value("${register.code.ip-window-seconds:3600}")
    private int windowSeconds;

    @Override
    public boolean tryAcquire(String ip) {
        String key = KEY_PREFIX + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            // Redis 异常时放行：验证码还有单邮箱 60s 冷却兜底，限流器故障不应阻断正常用户
            return true;
        }
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count <= ipLimit;
    }
}
