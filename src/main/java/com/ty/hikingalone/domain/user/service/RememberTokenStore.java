package com.ty.hikingalone.domain.user.service;

import java.time.Duration;

/**
 * 记住我（remember-me）持久登录令牌领域端口：令牌的生成与存储是技术细节，
 * 领域层只依赖此语义抽象；基础设施层提供 Redis 实现（{@code RedisRememberTokenStore}）。
 * <p>与 {@link PasswordHasher}、VerificationCodeStore 同一分层先例。语义约束：</p>
 * <ul>
 *   <li><b>每用户单令牌</b>：同账号再次 issue 会作废旧令牌——与会话互踢策略一致，
 *       被踢一端的旧令牌随之失效，避免两端凭令牌互相自动重登的乒乓</li>
 *   <li><b>滑动续期</b>：resolve 命中即续期，活跃用户不掉线；不活跃到期后需重新登录</li>
 * </ul>
 */
public interface RememberTokenStore {

    /** 令牌有效期（滑动）：Cookie max-age 与 Redis TTL 共用此策略 */
    Duration TOKEN_TTL = Duration.ofDays(30);

    /**
     * 为用户签发新令牌（作废其旧令牌），返回令牌本体（写入 HttpOnly Cookie）
     */
    String issue(Long userId);

    /**
     * 校验令牌：有效返回 userId 并滑动续期；无效/已过期/已被替换返回 null
     */
    Long resolve(String token);

    /**
     * 作废该用户的令牌（登出）
     */
    void revoke(Long userId);
}
