package com.ty.hikingalone.domain.user.entity;

import com.ty.hikingalone.domain.user.service.PasswordHasher;
import com.ty.hikingalone.infrastructure.user.service.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 密码哈希策略单测：注册即哈希、双形态校验、存量明文透明升级、改密
 */
class UserAccountTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void registerStoresHashNotPlaintext() {
        UserAccount account = UserAccount.register("hiker", "s3cret!", "hiker@example.com", hasher);
        assertThat(account.getPassword()).isNotEqualTo("s3cret!").hasSize(60);
        assertThat(hasher.isHashed(account.getPassword())).isTrue();
        assertThat(account.verifyPassword("s3cret!", hasher)).isTrue();
        assertThat(account.verifyPassword("wrong", hasher)).isFalse();
    }

    @Test
    void samePasswordHashesDifferentlyEachRegister() {
        UserAccount a = UserAccount.register("a", "same-pw", null, hasher);
        UserAccount b = UserAccount.register("b", "same-pw", null, hasher);
        assertThat(a.getPassword()).isNotEqualTo(b.getPassword()); // BCrypt 随机盐
    }

    @Test
    void legacyPlaintextVerifiesThenUpgrades() {
        UserAccount legacy = UserAccount.reconstruct(1L, "old", "plain-pw",
                "old@example.com", null, null);
        assertThat(legacy.hasLegacyPassword(hasher)).isTrue();

        // 明文形态仍可登录
        assertThat(legacy.verifyPassword("plain-pw", hasher)).isTrue();
        assertThat(legacy.verifyPassword("wrong", hasher)).isFalse();

        // 透明升级：明文原值覆盖为哈希
        legacy.applyPasswordHash("plain-pw", hasher);
        assertThat(legacy.hasLegacyPassword(hasher)).isFalse();
        assertThat(legacy.getPassword()).isNotEqualTo("plain-pw");
        assertThat(legacy.verifyPassword("plain-pw", hasher)).isTrue();
    }

    @Test
    void changePasswordVerifiesOldAndHashesNew() {
        UserAccount account = UserAccount.register("hiker", "old-pw", null, hasher);

        assertThatThrownBy(() -> account.changePassword("bad", "new-pw", hasher))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("旧密码错误");

        account.changePassword("old-pw", "new-pw", hasher);
        assertThat(account.verifyPassword("new-pw", hasher)).isTrue();
        assertThat(account.verifyPassword("old-pw", hasher)).isFalse();
        assertThat(hasher.isHashed(account.getPassword())).isTrue();
    }
}
