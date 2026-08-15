package com.ty.hikingalone.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态码枚举
 */
@Getter
public enum ResultCode {
    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // HTTP 状态码对应的业务状态码
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token无效"),
    TOKEN_EXPIRED(1005, "Token已过期"),

    // 其他自定义状态码
    UPLOAD_FAILED(2001, "上传失败"),
    DOWNLOAD_FAILED(2002, "下载失败"),
    DATA_EXIST(2003, "数据已存在"),
    DATA_NOT_EXIST(2004, "数据不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
