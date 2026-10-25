package com.ty.hikingalone.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ty.hikingalone.common.session.UserSessionRegistry;
import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 登录拦截器 remember-me 重建路径单测：假令牌存储 + Mock 请求响应，不启 Spring 上下文
 */
class LoginUserInterceptorTest {

    /** 内存假令牌存储：复刻 Redis 实现的语义（每用户单令牌、issue 作废旧令牌） */
    static class FakeTokenStore implements RememberTokenStore {
        final Map<String, Long> tokenToUser = new HashMap<>();
        final Map<Long, String> userToToken = new HashMap<>();

        @Override
        public String issue(Long userId) {
            String old = userToToken.get(userId);
            if (old != null) tokenToUser.remove(old);
            String token = "tok-" + userId + "-" + System.nanoTime();
            tokenToUser.put(token, userId);
            userToToken.put(userId, token);
            return token;
        }

        @Override
        public Long resolve(String token) {
            return tokenToUser.get(token);
        }

        @Override
        public void revoke(Long userId) {
            String token = userToToken.remove(userId);
            if (token != null) tokenToUser.remove(token);
        }
    }

    private final FakeTokenStore tokenStore = new FakeTokenStore();
    private final LoginUserInterceptor interceptor = new LoginUserInterceptor(
            new ObjectMapper(), tokenStore, new UserSessionRegistry());

    @Test
    void noSessionNoCookieReturns401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request, response, new Object());

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void validSessionPassesWithoutCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSessionHolder.putLoginUser(request, 7L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request, response, new Object());

        assertThat(proceed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rememberCookieRebuildsSessionAfterExpiry() throws Exception {
        String token = tokenStore.issue(9L);

        // 模拟「会话过期 + 浏览器重开」：无 JSESSIONID，仅携带 remember Cookie
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request, response, new Object());

        assertThat(proceed).as("有效令牌应自动重建登录态").isTrue();
        assertThat(request.getSession().getAttribute(LoginUserInterceptor.SESSION_LOGIN_USER_ID))
                .isEqualTo(9L);
    }

    @Test
    void revokedTokenReturns401() throws Exception {
        String token = tokenStore.issue(9L);
        tokenStore.revoke(9L); // 登出已作废

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void tokenReplacedByNewLoginNoLongerRebuilds() throws Exception {
        String oldToken = tokenStore.issue(9L);
        tokenStore.issue(9L); // 同账号新登录替换令牌（踢端场景）

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(LoginUserInterceptor.REMEMBER_COOKIE, oldToken));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, new Object()))
                .as("被替换的旧令牌不得再自动登录").isFalse();
    }

    /** 测试辅助：向 Mock 会话写入登录用户 */
    private static final class MockHttpSessionHolder {
        static void putLoginUser(MockHttpServletRequest request, Long userId) {
            request.getSession(true).setAttribute(LoginUserInterceptor.SESSION_LOGIN_USER_ID, userId);
        }
    }
}
