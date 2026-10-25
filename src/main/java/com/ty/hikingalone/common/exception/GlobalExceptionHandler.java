package com.ty.hikingalone.common.exception;

import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;

/**
 * 全局异常处理：把各类异常统一转换为 Result 返回，保证接口格式一致
 * <p>body 中的 code 与 HTTP 状态码保持一致（401 拦截器先例）；前端 request.ts 只特判
 * HTTP 401 跳登录，其余按 body code/message 抛错，因此两处同时写值是兼容的。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（@Valid 触发的 MethodArgumentNotValidException）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e, HttpServletResponse response) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 日期格式错误
     */
    @ExceptionHandler(DateTimeParseException.class)
    public Result<Void> handleDateParse(DateTimeParseException e, HttpServletResponse response) {
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "日期格式错误，应为 yyyy-MM-dd");
    }

    /**
     * 请求体缺失或 JSON 格式错误（如漏 body、多逗号）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "请求体缺失或格式错误");
    }

    /**
     * 参数类型不匹配（如 /journal/{id} 传 abc）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletResponse response) {
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "参数类型错误：" + e.getName());
    }

    /**
     * 缺少必填 query 参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e, HttpServletResponse response) {
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 业务校验不通过（如计划/训练项不存在、日期范围非法）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e, HttpServletResponse response) {
        log.warn("业务参数错误: {}", e.getMessage());
        response.setStatus(ResultCode.BAD_REQUEST.getCode());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 请求方法不支持（如对 POST-only 接口发 GET）。
     * <p>Boot 3 中路径存在但方法不匹配同样会走到这里，必须先于兜底处理，否则会被报成 500。</p>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletResponse response) {
        response.setStatus(ResultCode.METHOD_NOT_ALLOWED.getCode());
        return Result.error(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * 接口不存在（404）。
     * <p>Boot 3.2+ 无 handler 的路径由静态资源兜底 handler 抛 {@link NoResourceFoundException}；
     * 若不单独处理会被兜底 Exception 处理器吞成 500，扫描器流量也会刷 error 日志。</p>
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e, HttpServletResponse response) {
        log.warn("接口不存在: {}", e.getResourcePath());
        response.setStatus(ResultCode.NOT_FOUND.getCode());
        return Result.error(ResultCode.NOT_FOUND.getCode(), "接口不存在");
    }

    /**
     * 兜底：未预期异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        log.error("系统异常", e);
        response.setStatus(ResultCode.ERROR.getCode());
        return Result.error(ResultCode.ERROR);
    }
}
