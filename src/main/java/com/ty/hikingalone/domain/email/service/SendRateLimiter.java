package com.ty.hikingalone.domain.email.service;

/**
 * 发送频控领域端口：按来源 IP 限制验证码发送频率（防刷——逐个换邮箱可绕过单邮箱冷却，
 * IP 维度配额封住这条路径）。基础设施层提供 Redis 计数窗口实现（{@code RedisSendRateLimiter}）。
 */
public interface SendRateLimiter {

    /**
     * 尝试获取一次发送配额：窗口内未超限则计数 +1 并返回 true；超限返回 false（不计数）
     */
    boolean tryAcquire(String ip);
}
