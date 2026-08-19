package com.ty.hikingalone.interfaces.user;

import com.ty.hikingalone.application.user.UserService;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.user.dto.command.UserChangePasswordDTO;
import com.ty.hikingalone.interfaces.user.dto.command.UserCreateDTO;
import com.ty.hikingalone.interfaces.user.dto.query.UserLoginDTO;
import com.ty.hikingalone.interfaces.user.vo.command.UserChangePasswordVO;
import com.ty.hikingalone.interfaces.user.vo.command.UserCreateVO;
import com.ty.hikingalone.interfaces.user.vo.query.UserLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserCreateVO> register(@Validated UserCreateDTO userCreateDTO) {
        UserCreateVO register = userService.register();
        return Result.success(register);
    }

    @GetMapping("/login")
    public Result<UserLoginVO> login(@Validated UserLoginDTO userLoginDTO) {
        UserLoginVO login = userService.login();
        return Result.success(login);
    }

    @PostMapping("/forget")
    public Result<UserChangePasswordVO> changePassword(@Validated UserChangePasswordDTO userChangePasswordDTO) {
        UserChangePasswordVO changePassword = userService.changePassword();
        return Result.success(changePassword);
    }

}
