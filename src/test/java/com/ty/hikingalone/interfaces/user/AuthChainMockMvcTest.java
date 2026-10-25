package com.ty.hikingalone.interfaces.user;

import com.ty.hikingalone.common.interceptor.LoginUserInterceptor;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.repository.UserAccountRepository;
import com.ty.hikingalone.domain.user.service.PasswordHasher;
import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import com.ty.hikingalone.testsupport.InMemoryRememberTokenStore;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证链路 MockMvc 集成测试：真实拦截器/控制器/会话登记表 + 真实 MySQL，
 * 仅 RememberTokenStore 用内存实现替代 Redis（本机与 CI 均不依赖 Redis 服务）。
 * 覆盖：未登录 401、登录互踢、remember-me 令牌重建会话、登出作废令牌。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthChainMockMvcTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PasswordHasher passwordHasher;

    @TestConfiguration
    static class TokenStoreOverride {
        /** 内存版：语义与 RedisRememberTokenStore 一致（每用户单令牌） */
        @Bean
        @Primary
        RememberTokenStore inMemoryRememberTokenStore() {
            return new InMemoryRememberTokenStore();
        }
    }

    private static final String PASSWORD = "pw-123456";

    private String createUser() {
        String email = "it-" + System.nanoTime() + "@integration.test";
        String username = "it" + System.nanoTime();
        userAccountRepository.save(UserAccount.register(username, PASSWORD, email, passwordHasher));
        return email;
    }

    private static String loginBody(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}";
    }

    @Test
    void protectedApiWithoutLoginReturns401() throws Exception {
        mockMvc.perform(get("/user/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void loginKicksPreviousSession() throws Exception {
        String email = createUser();
        MockHttpSession sessionA = new MockHttpSession();
        MockHttpSession sessionB = new MockHttpSession();

        mockMvc.perform(post("/user/login").session(sessionA).contentType(APPLICATION_JSON).content(loginBody(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/user/login").session(sessionB).contentType(APPLICATION_JSON).content(loginBody(email)))
                .andExpect(status().isOk());

        // A 的会话被踢：下一次请求 401；B 存活
        mockMvc.perform(get("/user/me").session(sessionA)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/me").session(sessionB)).andExpect(status().isOk());
    }

    @Test
    void rememberCookieRebuildsSessionWithoutJsessionId() throws Exception {
        String email = createUser();
        MvcResult login = mockMvc.perform(
                        post("/user/login").contentType(APPLICATION_JSON).content(loginBody(email)))
                .andExpect(status().isOk())
                .andReturn();

        String token = login.getResponse().getHeaders("Set-Cookie").stream()
                .filter(c -> c.startsWith(LoginUserInterceptor.REMEMBER_COOKIE + "="))
                .findFirst().orElse(null);
        assertThat(token).as("登录应下发 remember Cookie").isNotNull();
        String tokenValue = token.split("=")[1].split(";")[0];

        // 模拟浏览器重开/会话过期：仅携带 remember Cookie、无 JSESSIONID
        mockMvc.perform(get("/user/me").cookie(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, tokenValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));
    }

    @Test
    void logoutRevokesRememberToken() throws Exception {
        String email = createUser();
        MockHttpSession session = new MockHttpSession();
        MvcResult login = mockMvc.perform(
                        post("/user/login").session(session).contentType(APPLICATION_JSON).content(loginBody(email)))
                .andExpect(status().isOk())
                .andReturn();
        String token = login.getResponse().getHeaders("Set-Cookie").stream()
                .filter(c -> c.startsWith(LoginUserInterceptor.REMEMBER_COOKIE + "="))
                .findFirst().orElseThrow().split("=")[1].split(";")[0];

        mockMvc.perform(post("/user/logout").session(session)
                        .cookie(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, token)))
                .andExpect(status().isOk());

        // 登出作废令牌：仅凭旧令牌不得自动重登
        mockMvc.perform(get("/user/me").cookie(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, token)))
                .andExpect(status().isUnauthorized());
    }
}
