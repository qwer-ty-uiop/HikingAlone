package com.ty.hikingalone.infrastructure.email.repository.impl;

import com.ty.hikingalone.domain.email.entity.VerificationCode;
import com.ty.hikingalone.domain.email.repository.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 验证码存储的 Redis 实现（基础设施层）：
 * 领域接口 {@link VerificationCodeStore} 只声明语义操作，Redis 的 key/value/过期时间
 * 等实现细节全部收敛在本类；后续若换缓存中间件（如 Caffeine/本地内存），只需新增一个实现类，
 * 应用层与领域层零改动。
 * <p>key 由领域实体 {@link VerificationCode} 计算（email 维度，带 email:code:/email:cool: 前缀），
 * 本类只做真正的存取。</p>
 */
@Repository
@RequiredArgsConstructor
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isCooling(VerificationCode code) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(code.getCoolKey()));
    }

    @Override
    public void markCooling(VerificationCode code, int coolSeconds) {
        // 冷却 key 只占位（value 为空串），存在即表示冷却中
        redisTemplate.opsForValue().set(code.getCoolKey(), "", coolSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void saveCode(VerificationCode code, int expireSeconds) {
        // value 是验证码本体，供后续 verify 比对
        redisTemplate.opsForValue().set(code.getCodeKey(), code.getCode(), expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getCode(VerificationCode code) {
        return redisTemplate.opsForValue().get(code.getCodeKey());
    }

    @Override
    public void removeCode(VerificationCode code) {
        redisTemplate.delete(code.getCodeKey());
    }
}
