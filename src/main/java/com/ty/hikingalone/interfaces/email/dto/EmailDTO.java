package com.ty.hikingalone.interfaces.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 发送验证码请求体
 * <p>@NotBlank 非空 + @Email 格式校验；需配合 @RequestBody @Valid 才触发校验</p>
 */
@Data
@Builder
public class EmailDTO {

    /** 目标邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

}
