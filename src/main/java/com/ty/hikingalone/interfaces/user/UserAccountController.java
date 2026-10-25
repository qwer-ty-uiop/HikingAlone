package com.ty.hikingalone.interfaces.user;

import com.ty.hikingalone.application.user.UserService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.interceptor.LoginUserInterceptor;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.session.UserSessionRegistry;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.domain.user.service.RememberTokenStore;
import com.ty.hikingalone.interfaces.user.converter.UserAccountConverter;
import com.ty.hikingalone.interfaces.user.dto.command.UserChangePasswordDTO;
import com.ty.hikingalone.interfaces.user.dto.command.UserCreateDTO;
import com.ty.hikingalone.interfaces.user.dto.query.UserLoginDTO;
import com.ty.hikingalone.interfaces.user.vo.command.UserCreateVO;
import com.ty.hikingalone.interfaces.user.vo.query.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserService userService;
    private final UserAccountConverter converter;
    private final UserSessionRegistry userSessionRegistry;
    private final RememberTokenStore rememberTokenStore;

    @PostMapping("/register")
    public Result<UserCreateVO> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserAccount account = userService.register(converter.toRegisterCmd(userCreateDTO));
        return Result.success(converter.toUserCreateVO(account));
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO,
                                     HttpServletRequest request, HttpServletResponse response) {
        UserAccount account = userService.login(converter.toLoginCmd(userLoginDTO));
        // 登录成功：建立服务端会话，供 /train 等需登录接口从会话取当前用户；
        // 同账号单活跃会话：新登录踢掉旧端（旧端下次请求 401，走「登录已过期」链路）
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginUserInterceptor.SESSION_LOGIN_USER_ID, account.getId());
        userSessionRegistry.activate(account.getId(), session);
        // remember-me：签发持久登录令牌（作废旧令牌）。HttpOnly 防 XSS 读取、SameSite=Lax 防跨站
        // 携带；不加 Secure——生产当前为 HTTP，加了 Cookie 不会被发送。会话过期后拦截器凭此重建登录态
        response.addHeader(HttpHeaders.SET_COOKIE, rememberCookie(rememberTokenStore.issue(account.getId())).toString());
        return Result.success(converter.toUserLoginVO(account));
    }

    @PostMapping("/forget")
    public Result<Void> changePassword(@Valid @RequestBody UserChangePasswordDTO userChangePasswordDTO) {
        userService.changePassword(converter.toChangePasswordCmd(userChangePasswordDTO));
        return Result.success();
    }

    /**
     * 当前登录用户信息（会话校验接口）：前端页面加载时调用它校准登录态。
     * <p>由 LoginUserInterceptor 拦截，未登录直接返回 401；已登录则从会话上下文取 userId 查账号返回邮箱，
     * 前端据此确认"本地认为已登录"是否与服务端一致，杜绝会话失效后仍显示已登录的闪屏/反复跳转。</p>
     */
    @GetMapping("/me")
    public Result<UserLoginVO> me() {
        UserAccount account = userService.getUserById(LoginUserContext.getUserId());
        return Result.success(converter.toUserLoginVO(account));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 只清理仍归属本会话的登记；已被新登录踢掉的一端登出时不得误删新端的登记
            Object userId = session.getAttribute(LoginUserInterceptor.SESSION_LOGIN_USER_ID);
            if (userId instanceof Long id) {
                userSessionRegistry.deactivate(id, session);
                // 登出同时作废 remember-me 令牌并过期 Cookie：关浏览器重开不得再自动登录
                rememberTokenStore.revoke(id);
            }
            session.invalidate();
        }
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRememberCookie().toString());
        return Result.success();
    }

    /** remember-me Cookie：HttpOnly + SameSite=Lax，max-age 与令牌 TTL 一致（端口常量共用） */
    private static ResponseCookie rememberCookie(String token) {
        return ResponseCookie.from(LoginUserInterceptor.REMEMBER_COOKIE, token)
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(RememberTokenStore.TOKEN_TTL)
                .path("/")
                .build();
    }

    /** 过期 remember-me Cookie（登出清理浏览器侧） */
    private static ResponseCookie expiredRememberCookie() {
        return ResponseCookie.from(LoginUserInterceptor.REMEMBER_COOKIE, "")
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();
    }

}
