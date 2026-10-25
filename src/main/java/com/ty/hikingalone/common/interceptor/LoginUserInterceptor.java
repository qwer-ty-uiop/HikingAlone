package com.ty.hikingalone.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.result.ResultCode;
import com.ty.hikingalone.common.session.UserSessionRegistry;
import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录拦截器：校验 HttpSession 中是否存在登录用户 id。
 * <p>会话缺失/过期时尝试 remember-me 自动重建登录态：Cookie 持有有效令牌则重建会话
 * （按单活跃会话登记，与正常登录等价）；令牌也无效才返回 401。</p>
 * <p>未登录时返回 Result.error(401) 并终止请求；已登录则把 userId 写入 LoginUserContext。</p>
 */
@Component
@RequiredArgsConstructor
public class LoginUserInterceptor implements HandlerInterceptor {

    /** 会话中登录用户 id 的 key（与 UserController 登录时写入保持一致） */
    public static final String SESSION_LOGIN_USER_ID = "loginUserId";

    /** remember-me 持久登录令牌的 Cookie 名（Controller 发/废令牌与此保持一致） */
    public static final String REMEMBER_COOKIE = "hikingalone.remember";

    private final ObjectMapper objectMapper;
    private final RememberTokenStore rememberTokenStore;
    private final UserSessionRegistry userSessionRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SESSION_LOGIN_USER_ID);
        if (userId == null) {
            // 会话缺失/过期：凭 remember-me 令牌自动重建（关闭浏览器重开/超时后免登录）
            Long remembered = resolveRememberedUser(request);
            if (remembered != null) {
                session = request.getSession(true);
                session.setAttribute(SESSION_LOGIN_USER_ID, remembered);
                userId = remembered;
                // 重建同样遵守单活跃会话：登记并踢掉该用户的旧会话（与正常登录一致）
                userSessionRegistry.activate(remembered, session);
            }
        }
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(ResultCode.UNAUTHORIZED)));
            return false;
        }
        LoginUserContext.setUserId((Long) userId);
        return true;
    }

    /**
     * 从 Cookie 中解析 remember-me 令牌并校验；无 Cookie 或令牌无效返回 null
     */
    private Long resolveRememberedUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (REMEMBER_COOKIE.equals(cookie.getName())) {
                return rememberTokenStore.resolve(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        LoginUserContext.clear();
    }
}
