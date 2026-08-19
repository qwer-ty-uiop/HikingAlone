package com.ty.hikingalone.domain.user.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户账号领域实体
 * <p>负责自身的创建与业务规则（密码校验/修改），不暴露可变字段随意修改</p>
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccount {

    private Long id;

    private String username;

    private String password;

    private String email;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：业务校验并创建一个待持久化的新账号
     */
    public static UserAccount register(String username, String password, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        UserAccount account = new UserAccount();
        account.username = username;
        account.password = password;
        account.email = email;
        return account;
    }

    /**
     * 校验密码是否匹配（登录/改密共用）
     */
    public boolean verifyPassword(String rawPassword) {
        return password.equals(rawPassword);
    }

    /**
     * 修改密码：校验旧密码后覆盖新密码，并刷新 updateTime
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("旧密码错误");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        this.password = newPassword;
        this.updateTime = LocalDateTime.now();
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