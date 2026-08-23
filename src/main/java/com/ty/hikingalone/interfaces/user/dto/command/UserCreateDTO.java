package com.ty.hikingalone.interfaces.user.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 注册请求体：用户名 + 密码 + 邮箱 + 邮箱验证码
 * <p>验证码用于证明邮箱归属，先经 /email/verify 校验或由 UserService 内部校验</p>
 */
@Data
@Builder
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
