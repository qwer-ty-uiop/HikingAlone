package com.ty.hikingalone.application.email.cmd;

/**
 * 发送验证码命令：email 目标邮箱；ip 客户端来源（IP 维度限流防刷用）
 */
public record GetCodeCmd(String email, String ip) {
}
