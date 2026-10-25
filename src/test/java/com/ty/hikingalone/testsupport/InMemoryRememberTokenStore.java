package com.ty.hikingalone.testsupport;

import com.ty.hikingalone.domain.user.service.RememberTokenStore;

import java.util.HashMap;
import java.util.Map;

/**
 * remember-me 令牌存储的内存实现（仅测试用）：语义与 RedisRememberTokenStore 一致——
 * 每用户单令牌（issue 作废旧令牌）。集成测试以 @Primary 覆盖 Redis 实现，
 * 使本机与 CI 均无需 Redis 服务即可跑认证链路。
 */
public class InMemoryRememberTokenStore implements RememberTokenStore {

    private final Map<String, Long> tokenToUser = new HashMap<>();
    private final Map<Long, String> userToToken = new HashMap<>();

    @Override
    public String issue(Long userId) {
        String old = userToToken.get(userId);
        if (old != null) {
            tokenToUser.remove(old);
        }
        String token = "test-token-" + userId + "-" + System.nanoTime();
        tokenToUser.put(token, userId);
        userToToken.put(userId, token);
        return token;
    }

    @Override
    public Long resolve(String token) {
        return tokenToUser.get(token);
    }

    @Override
    public void revoke(Long userId) {
        String token = userToToken.remove(userId);
        if (token != null) {
            tokenToUser.remove(token);
        }
    }
}
