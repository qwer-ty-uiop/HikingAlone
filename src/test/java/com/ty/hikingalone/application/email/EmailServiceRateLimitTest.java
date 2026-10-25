package com.ty.hikingalone.application.email;

import com.ty.hikingalone.application.email.cmd.GetCodeCmd;
import com.ty.hikingalone.domain.email.entity.VerificationCode;
import com.ty.hikingalone.domain.email.repository.VerificationCodeStore;
import com.ty.hikingalone.domain.email.service.SendRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 验证码发送 IP 限流单测：不启 Spring 上下文，假存储 + 假限流器 + 空邮件发送器
 */
class EmailServiceRateLimitTest {

    /** 内存验证码存储：仅记录被写入的 key */
    static class FakeStore implements VerificationCodeStore {
        final Set<String> keys = new HashSet<>();

        @Override public boolean isCooling(VerificationCode code) { return keys.contains(code.getCoolKey()); }
        @Override public void markCooling(VerificationCode code, int coolSeconds) { keys.add(code.getCoolKey()); }
        @Override public void saveCode(VerificationCode code, int expireSeconds) { keys.add(code.getCodeKey()); }
        @Override public String getCode(VerificationCode code) { return null; }
        @Override public void removeCode(VerificationCode code) { keys.remove(code.getCodeKey()); }
    }

    /** 可编程限流器：allow 开关 */
    static class SwitchableLimiter implements SendRateLimiter {
        boolean allow = true;
        @Override public boolean tryAcquire(String ip) { return allow; }
    }

    private final FakeStore store = new FakeStore();
    private final SwitchableLimiter limiter = new SwitchableLimiter();
    private final EmailService service = new EmailService(mock(JavaMailSender.class), store, limiter);

    @Test
    void overIpQuotaThrowsAndWritesNothing() {
        limiter.allow = false;

        assertThatThrownBy(() -> service.sendCode(new GetCodeCmd("a@b.test", "1.2.3.4")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("发送过于频繁，请稍后再试");
        // 超限请求不得写任何验证码/冷却 key（不占用邮箱冷却、不产生脏数据）
        assertThat(store.keys).isEmpty();
    }

    @Test
    void withinQuotaSendsNormally() {
        boolean sent = service.sendCode(new GetCodeCmd("a@b.test", "1.2.3.4"));

        assertThat(sent).isTrue();
        assertThat(store.keys).isNotEmpty();
    }
}
