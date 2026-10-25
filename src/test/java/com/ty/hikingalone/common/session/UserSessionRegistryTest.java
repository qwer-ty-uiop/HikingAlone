package com.ty.hikingalone.common.session;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 同账号单活跃会话（互踢）登记表单测：不启 Spring 上下文，用 MockHttpSession 直接验证
 */
class UserSessionRegistryTest {

    private final UserSessionRegistry registry = new UserSessionRegistry();

    /** MockHttpSession 失效后任何访问抛 IllegalStateException，以此探测失效状态 */
    private static boolean invalidated(MockHttpSession session) {
        try {
            session.getAttribute("__probe__");
            return false;
        } catch (IllegalStateException e) {
            return true;
        }
    }

    @Test
    void secondLoginInvalidatesFirstSession() {
        MockHttpSession first = new MockHttpSession();
        MockHttpSession second = new MockHttpSession();

        registry.activate(1L, first);
        registry.activate(1L, second);

        assertThat(invalidated(first)).as("旧端会话应被踢掉").isTrue();
        assertThat(invalidated(second)).as("新端会话应存活").isFalse();
    }

    @Test
    void sameSessionReLoginDoesNotInvalidateItself() {
        MockHttpSession session = new MockHttpSession();
        registry.activate(1L, session);
        registry.activate(1L, session);

        assertThat(invalidated(session)).as("同一会话重复登录不得自我失效").isFalse();
    }

    @Test
    void expiredPreviousSessionIsTolerated() {
        MockHttpSession stale = new MockHttpSession();
        registry.activate(1L, stale);
        stale.invalidate(); // 模拟 30 分钟自然过期后再在另一端登录

        MockHttpSession fresh = new MockHttpSession();
        registry.activate(1L, fresh); // 不应抛 IllegalStateException

        assertThat(invalidated(fresh)).isFalse();
    }

    @Test
    void logoutRemovesOnlyOwnEntry() {
        MockHttpSession old = new MockHttpSession();
        MockHttpSession fresh = new MockHttpSession();
        registry.activate(1L, old);
        registry.activate(1L, fresh); // fresh 踢掉 old，登记归属 fresh

        // 被踢的 old 端随后登出：不得误删 fresh 的登记（否则 fresh 变孤儿，永不失效）
        registry.deactivate(1L, old);
        MockHttpSession third = new MockHttpSession();
        registry.activate(1L, third);
        assertThat(invalidated(fresh)).as("old 端登出不得动 fresh 的登记").isTrue();
        assertThat(invalidated(third)).isFalse();

        // 正常登出后登记被清理：下一次登录不应误踢任何人
        UserSessionRegistry r = new UserSessionRegistry();
        MockHttpSession a = new MockHttpSession();
        r.activate(1L, a);
        r.deactivate(1L, a);
        MockHttpSession b = new MockHttpSession();
        r.activate(1L, b);
        assertThat(invalidated(b)).as("a 已正常登出，b 登录不应有人被踢").isFalse();
    }
}
