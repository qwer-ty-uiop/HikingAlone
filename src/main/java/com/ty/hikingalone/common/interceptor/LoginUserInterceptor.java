package com.ty.hikingalone.common.interceptor;

import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录拦截器：校验 HttpSession 中是否存在登录用户 id。
 * <p>未登录时返回 Result.error(401, "未登录") 并终止请求；已登录则把 userId 写入 LoginUserContext。</p>
 */
@Component
@RequiredArgsConstructor
public class LoginUserInterceptor implements HandlerInterceptor {

    /** 会话中登录用户 id 的 key（与 UserController 登录时写入保持一致） */
    public static final String SESSION_LOGIN_USER_ID = "loginUserId";

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute(SESSION_LOGIN_USER_ID);
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        LoginUserContext.clear();
    }
}
