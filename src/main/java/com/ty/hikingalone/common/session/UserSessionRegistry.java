package com.ty.hikingalone.common.session;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户活跃会话登记表：同账号并发登录控制（单活跃会话，新登录踢旧端）
 * <p>登录成功时 {@link #activate}：登记新会话并使旧会话失效——旧端 Cookie 指向的会话
 * 已死，下一次请求 {@code getSession(false)} 为 null，由 LoginUserInterceptor 统一返回 401，
 * 前端走「登录已过期」跳转链路，与超时过期表现一致。</p>
 * <p>实现为进程内 ConcurrentHashMap：单机部署下与 Tomcat 内存会话同生命周期
 * （重启同时清空，天然一致）；不引 spring-session，保持学习项目依赖最小。
 * 被踢/过期会话的残留引用由下次登录覆盖或登出清理，量级为用户数，可忽略。</p>
 */
@Component
public class UserSessionRegistry {

    private final Map<Long, HttpSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * 登记登录用户的当前会话；若该用户已有旧会话（另一端登录）则将其失效
     */
    public void activate(Long userId, HttpSession session) {
        HttpSession previous = activeSessions.put(userId, session);
        if (previous != null && previous != session) {
            try {
                previous.invalidate();
            } catch (IllegalStateException ignored) {
                // 旧会话已自然过期/失效，无需再踢
            }
        }
    }

    /**
     * 登出清理：仅当登记的仍是本会话时移除（compare-and-remove）。
     * 已被新登录踢掉的一端登出时，登记已归属新会话，不得误删。
     */
    public void deactivate(Long userId, HttpSession session) {
        activeSessions.remove(userId, session);
    }
}
