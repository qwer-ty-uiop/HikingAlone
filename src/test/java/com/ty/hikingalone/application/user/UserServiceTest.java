package com.ty.hikingalone.application.user;

import com.ty.hikingalone.application.email.EmailService;
import com.ty.hikingalone.application.email.cmd.VerifyCodeCmd;
import com.ty.hikingalone.application.user.cmd.UserLoginCmd;
import com.ty.hikingalone.application.user.cmd.UserRegisterCmd;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import com.ty.hikingalone.domain.user.service.PasswordHasher;
import com.ty.hikingalone.infrastructure.user.service.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 登录/注册应用服务单测：重点验证存量明文「首次登录比对成功 → 透明升级为哈希并落库」，
 * 用内存假仓储 + 真实 BCrypt 哈希器，不启动 Spring 上下文
 */
class UserServiceTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher();
    private final FakeRepository repo = new FakeRepository();
    private final UserService userService = new UserService(repo, stubEmailService(), hasher);

    /** 邮箱验证码直接放行（注册/改密流程前置校验，与密码哈希无关） */
    private EmailService stubEmailService() {
        return new EmailService(null, null, null) {
            @Override
            public boolean verify(VerifyCodeCmd cmd) {
                return true;
            }
        };
    }

    @Test
    void registerPersistsHashedPassword() {
        UserAccount account = userService.register(
                new UserRegisterCmd("hiker", "s3cret!", "hiker@example.com", "000000"));
        String stored = repo.byEmail("hiker@example.com").getPassword();
        assertThat(stored).isNotEqualTo("s3cret!");
        assertThat(hasher.isHashed(stored)).isTrue();
        assertThat(account.getId()).isNotNull();
    }

    @Test
    void legacyPlaintextLoginUpgradesToHashAndPersists() {
        // 预置一个存量明文账号（旧库形态）
        UserAccount legacy = UserAccount.reconstruct(null, "old-user", "plain-pw",
                "old@example.com", null, null);
        repo.save(legacy);

        // 首次登录：明文比对成功 + 透明升级落库
        UserAccount login = userService.login(new UserLoginCmd("old@example.com", "plain-pw"));
        assertThat(login.getUsername()).isEqualTo("old-user");
        String stored = repo.byEmail("old@example.com").getPassword();
        assertThat(hasher.isHashed(stored)).as("登录后库里应为哈希").isTrue();

        // 再次登录：走哈希校验路径同样成功
        assertThat(userService.login(new UserLoginCmd("old@example.com", "plain-pw"))
                .getUsername()).isEqualTo("old-user");
    }

    @Test
    void wrongPasswordFailsWithoutUpgrading() {
        UserAccount legacy = UserAccount.reconstruct(null, "old-user", "plain-pw",
                "old@example.com", null, null);
        repo.save(legacy);

        assertThatThrownBy(() -> userService.login(new UserLoginCmd("old@example.com", "wrong")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("邮箱或密码错误");
        assertThat(repo.byEmail("old@example.com").getPassword())
                .as("失败登录不得改写存量明文").isEqualTo("plain-pw");
    }

    @Test
    void unknownEmailFailsWithSameMessage() {
        assertThatThrownBy(() -> userService.login(new UserLoginCmd("ghost@example.com", "x")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("邮箱或密码错误");
    }

    /** 内存假仓储：只实现用到的读写，未用到的方法抛不支持 */
    private static class FakeRepository implements UserAccountRepository {
        private final Map<String, UserAccount> byEmailIndex = new ConcurrentHashMap<>();
        private final AtomicLong idGen = new AtomicLong();

        @Override
        public Long save(UserAccount account) {
            account.setId(idGen.incrementAndGet());
            byEmailIndex.put(account.getEmail(), account);
            return account.getId();
        }

        @Override
        public Long update(UserAccount account) {
            byEmailIndex.put(account.getEmail(), account);
            return 1L;
        }

        @Override
        public Long delete(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserAccount findById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public UserAccount findByUsername(String username) {
            return null;
        }

        @Override
        public UserAccount findByEmail(String email) {
            return byEmailIndex.get(email);
        }

        @Override
        public List<UserAccount> findAll() {
            throw new UnsupportedOperationException();
        }

        UserAccount byEmail(String email) {
            return byEmailIndex.get(email);
        }
    }
}
