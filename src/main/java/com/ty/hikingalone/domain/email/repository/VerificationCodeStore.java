package com.ty.hikingalone.domain.email.repository;

import com.ty.hikingalone.domain.email.entity.VerificationCode;

/**
 * 邮箱验证码存储仓库接口（领域层）：
 * 只定义「存/取验证码、存/查冷却标记」这些业务语义，不暴露任何存储技术细节
 * （Redis key、过期实现等一律不出现）。Redis 实现见基础设施层
 * {@code RedisVerificationCodeStore}。
 * <p>方法以 {@link VerificationCode} 实体为参数——key 的拼接属于领域实体职责
 * （VerificationCode 持有 codeKey/coolKey），仓库只负责持久化。</p>
 */
public interface VerificationCodeStore {

    /**
     * 是否处于发送冷却期（冷却标记存在即冷却中，防止同一邮箱频繁发送）
     *
     * @return true=冷却中，此时不应再次发送
     */
    boolean isCooling(VerificationCode code);

    /**
     * 写入发送冷却标记，coolSeconds 秒后自动过期（key 只占位，无业务 value）
     */
    void markCooling(VerificationCode code, int coolSeconds);

    /**
     * 保存验证码本体，expireSeconds 秒后自动过期
     */
    void saveCode(VerificationCode code, int expireSeconds);

    /**
     * 取出验证码本体；未发送过或已过期返回 null
     */
    String getCode(VerificationCode code);

    /**
     * 删除验证码（一次性消费：校验通过后立即删除，防止重放）
     */
    void removeCode(VerificationCode code);
}
