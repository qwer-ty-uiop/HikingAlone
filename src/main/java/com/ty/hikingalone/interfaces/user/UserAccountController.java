package com.ty.hikingalone.interfaces.user;

import com.ty.hikingalone.application.user.UserService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.interceptor.LoginUserInterceptor;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.interfaces.user.converter.UserAccountConverter;
import com.ty.hikingalone.interfaces.user.dto.command.UserChangePasswordDTO;
import com.ty.hikingalone.interfaces.user.dto.command.UserCreateDTO;
import com.ty.hikingalone.interfaces.user.dto.query.UserLoginDTO;
import com.ty.hikingalone.interfaces.user.vo.command.UserCreateVO;
import com.ty.hikingalone.interfaces.user.vo.query.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserService userService;
    private final UserAccountConverter converter;

    @PostMapping("/register")
    public Result<UserCreateVO> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserAccount account = userService.register(converter.toRegisterCmd(userCreateDTO));
        return Result.success(converter.toUserCreateVO(account));
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO userLoginDTO,
                                     HttpServletRequest request) {
        UserAccount account = userService.login(converter.toLoginCmd(userLoginDTO));
        // 登录成功：建立服务端会话，供 /train 等需登录接口从会话取当前用户
        HttpSession session = request.getSession(true);
        session.setAttribute(LoginUserInterceptor.SESSION_LOGIN_USER_ID, account.getId());
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
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success();
    }

}
