package com.ty.hikingalone.infrastructure.user.service;

import com.ty.hikingalone.domain.user.service.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * BCrypt 密码哈希实现（spring-security-crypto，默认强度 10，输出 $2a$10$… 共 60 字符）
 * <p>只引入 crypto 模块：无 security starter 的过滤链与自动配置，Web 行为不变。</p>
 */
@Component
public class BCryptPasswordHasher implements PasswordHasher {

    /** BCrypt 输出格式：$2a/$2b/$2y + 两位成本 + 22 位盐 + 31 位校验和，固定 60 字符 */
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    @Override
    public boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.length() == 60
                && BCRYPT_PATTERN.matcher(storedPassword).matches();
    }
}
