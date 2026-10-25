package com.ty.hikingalone.infrastructure.user.service;

import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;

/**
 * 记住我令牌存储的 Redis 实现（基础设施层）：
 * <ul>
 *   <li>{@code auth:remember:token:<token>} → userId：Cookie 令牌的校验入口</li>
 *   <li>{@code auth:remember:user:<userId>} → token：每用户单令牌的登记，issue/revoke 据此作废旧令牌</li>
 * </ul>
 * 双 key 同 TTL（{@link RememberTokenStore#TOKEN_TTL}，30 天滑动）；令牌为 SecureRandom
 * 32 字节 hex（不可枚举）。Redis 里存令牌本体——被拖库可伪造，学习项目接受该权衡
 * （生产可改为存 SHA-256 摘要）。
 */
@Repository
@RequiredArgsConstructor
public class RedisRememberTokenStore implements RememberTokenStore {

    private static final String KEY_TOKEN = "auth:remember:token:";
    private static final String KEY_USER = "auth:remember:user:";

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String issue(Long userId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        // 每用户单令牌：先作废旧令牌，再登记新令牌（双 key 同 TTL）
        String old = redisTemplate.opsForValue().get(KEY_USER + userId);
        if (old != null) {
            redisTemplate.delete(KEY_TOKEN + old);
        }
        redisTemplate.opsForValue().set(KEY_TOKEN + token, String.valueOf(userId), TOKEN_TTL);
        redisTemplate.opsForValue().set(KEY_USER + userId, token, TOKEN_TTL);
        return token;
    }

    @Override
    public Long resolve(String token) {
        String userId = redisTemplate.opsForValue().get(KEY_TOKEN + token);
        if (userId == null) {
            return null;
        }
        // 滑动续期：活跃用户不掉线
        redisTemplate.expire(KEY_TOKEN + token, TOKEN_TTL);
        redisTemplate.expire(KEY_USER + userId, TOKEN_TTL);
        return Long.valueOf(userId);
    }

    @Override
    public void revoke(Long userId) {
        String token = redisTemplate.opsForValue().get(KEY_USER + userId);
        if (token != null) {
            redisTemplate.delete(KEY_TOKEN + token);
        }
        redisTemplate.delete(KEY_USER + userId);
    }
}
