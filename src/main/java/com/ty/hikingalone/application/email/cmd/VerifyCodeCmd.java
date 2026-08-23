package com.ty.hikingalone.application.email.cmd;

/**
 * 校验验证码应用层命令：邮箱 + 用户输入的验证码
 */
public record VerifyCodeCmd(String email, String code) {
}
