package com.ty.hikingalone.interfaces.user.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChangePasswordDTO {
    @Email
    private String email;
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
    /** 邮箱验证码：证明邮箱归属（先发验证码再改密），4 位数字 */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "\\d{4}", message = "验证码为 4 位数字")
    private String code;
}
