package com.ty.hikingalone.interfaces.email;

import com.ty.hikingalone.domain.email.entity.VerificationCode;
import com.ty.hikingalone.domain.email.repository.VerificationCodeStore;
import com.ty.hikingalone.domain.email.service.SendRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /email/code IP 限流接线集成测试：真实控制器/服务/异常处理链路，
 * 仅验证码存储与限流器用内存实现、邮件发送器 mock（不依赖 Redis 与 SMTP）——
 * 锁定「限流器拒绝 → 400『发送过于频繁』」的端到端行为（HTTP 状态码与 body code 一致）。
 * <p>生产端到端备注：本机出口为运营商 NAT 轮换 IP，逐请求 IP 不同导致线上配额键分散，
 * 无法从本机验证生产配额；语义由单测（RedisSendRateLimiterTest）与本接线测试共同覆盖。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmailCodeRateLimitMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaMailSender mailSender;

    @TestConfiguration
    static class EmailOverrides {
        @Bean
        @Primary
        VerificationCodeStore inMemoryCodeStore() {
            return new InMemoryCodeStore();
        }

        /** 计数假限流器：每 IP 放行 2 次，第 3 次起拒绝 */
        @Bean
        @Primary
        SendRateLimiter countingLimiter() {
            return new CountingLimiter(2);
        }
    }

    static class InMemoryCodeStore implements VerificationCodeStore {
        final Set<String> keys = new HashSet<>();

        @Override public boolean isCooling(VerificationCode code) { return keys.contains(code.getCoolKey()); }
        @Override public void markCooling(VerificationCode code, int coolSeconds) { keys.add(code.getCoolKey()); }
        @Override public void saveCode(VerificationCode code, int expireSeconds) { keys.add(code.getCodeKey()); }
        @Override public String getCode(VerificationCode code) { return null; }
        @Override public void removeCode(VerificationCode code) { keys.remove(code.getCodeKey()); }
    }

    static class CountingLimiter implements SendRateLimiter {
        private final int limit;
        private int count = 0;

        CountingLimiter(int limit) { this.limit = limit; }

        @Override public boolean tryAcquire(String ip) { return ++count <= limit; }
    }

    @Test
    void overQuotaReturns400WithMessage() throws Exception {
        String body = "{\"email\":\"quota@test.example\"}";

        mockMvc.perform(post("/email/code").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sent").value(true));
        mockMvc.perform(post("/email/code").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        // 第 3 次被限流器拒绝：HTTP 400 + body code 400 + 明确文案，不触碰邮件/存储
        mockMvc.perform(post("/email/code").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("发送过于频繁，请稍后再试"));
    }
}
