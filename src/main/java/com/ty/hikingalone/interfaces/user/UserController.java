package com.ty.hikingalone.interfaces.user;

import com.ty.hikingalone.application.user.UserService;
import com.ty.hikingalone.common.interceptor.LoginUserInterceptor;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.domain.user.entity.UserAccount;
import com.ty.hikingalone.interfaces.user.converter.UserConverter;
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
public class UserController {

    private final UserService userService;
    private final UserConverter converter;

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

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success();
    }

}
