package com.ty.hikingalone.interfaces.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 校验验证码请求体：邮箱 + 用户输入的验证码
 */
@Data
@Builder
public class EmailVerifyDTO {

    /** 目标邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 用户输入的验证码 */
    @NotBlank(message = "验证码不能为空")
    private String code;

}
