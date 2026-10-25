package com.ty.hikingalone.domain.user.entity;

import com.ty.hikingalone.domain.user.service.PasswordHasher;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户账号领域实体
 * <p>负责自身的创建与业务规则（密码校验/修改/存量明文透明升级），不暴露可变字段随意修改。
 * 密码存储形态有两种，{@code verifyPassword} 对两种都成立：</p>
 * <ul>
 *   <li>BCrypt 哈希（$2a$10$… 60 字符）——2026-10-25 起新写入的形态</li>
 *   <li>存量明文——历史上已落库的账号，首次登录/改密比对成功后由应用服务调
 *       {@link #applyPasswordHash} 透明升级为哈希（免停机迁移，升级后旧明文即被覆盖）</li>
 * </ul>
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccount {

    private Long id;

    private String username;

    /** BCrypt 哈希，或待透明升级的存量明文 */
    private String password;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：业务校验并创建一个待持久化的新账号（密码经 hasher 哈希后存储）
     */
    public static UserAccount register(String username, String rawPassword, String email,
                                       PasswordHasher hasher) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        UserAccount account = new UserAccount();
        account.username = username;
        account.password = hasher.hash(rawPassword);
        account.email = email;
        return account;
    }

    /**
     * 校验密码是否匹配（登录/改密共用，哈希与存量明文两种形态都支持）
     */
    public boolean verifyPassword(String rawPassword, PasswordHasher hasher) {
        if (hasher.isHashed(password)) {
            return hasher.matches(rawPassword, password);
        }
        return password.equals(rawPassword);
    }

    /**
     * 是否为待透明升级的存量明文密码
     */
    public boolean hasLegacyPassword(PasswordHasher hasher) {
        return !hasher.isHashed(password);
    }

    /**
     * 用明文原值覆盖为哈希：存量明文透明升级 / 改密共用
     */
    public void applyPasswordHash(String rawPassword, PasswordHasher hasher) {
        this.password = hasher.hash(rawPassword);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 修改密码：校验旧密码后覆盖为哈希，并刷新 updateTime
     */
    public void changePassword(String oldPassword, String newPassword, PasswordHasher hasher) {
        if (!verifyPassword(oldPassword, hasher)) {
            throw new IllegalArgumentException("旧密码错误");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        applyPasswordHash(newPassword, hasher);
    }

    /**
     * 重建工厂：从持久化数据恢复账号，不执行业务校验（数据已合法落库）
     */
    public static UserAccount reconstruct(Long id, String username, String password, String email,
                                          LocalDateTime createTime, LocalDateTime updateTime) {
        UserAccount account = new UserAccount();
        account.id = id;
        account.username = username;
        account.password = password;
        account.email = email;
        account.createTime = createTime;
        account.updateTime = updateTime;
        return account;
    }
}
