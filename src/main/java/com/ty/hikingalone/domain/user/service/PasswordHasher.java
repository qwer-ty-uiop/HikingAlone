package com.ty.hikingalone.domain.user.service;

/**
 * 密码哈希领域端口：哈希算法是技术细节，领域层只依赖此抽象
 * <p>基础设施层提供 BCrypt 实现（{@code BCryptPasswordHasher}）；与
 * {@code VerificationCodeStore} 同一分层先例：应用/领域层不感知具体算法与框架。</p>
 */
public interface PasswordHasher {

    /**
     * 把明文密码哈希为可持久化的密文（BCrypt 自带随机盐，同一明文每次结果不同）
     */
    String hash(String rawPassword);

    /**
     * 校验明文是否与存储的哈希匹配
     */
    boolean matches(String rawPassword, String hashedPassword);

    /**
     * 判断存储值是否已是哈希（用于识别存量明文，触发透明升级）
     */
    boolean isHashed(String storedPassword);
}
