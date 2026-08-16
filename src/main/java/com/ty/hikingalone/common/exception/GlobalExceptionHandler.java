package com.ty.hikingalone.common.exception;

import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

/**
 * 全局异常处理：把各类异常统一转换为 Result 返回，保证接口格式一致
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（@Valid 触发的 MethodArgumentNotValidException）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 日期格式错误
     */
    @ExceptionHandler(DateTimeParseException.class)
    public Result<Void> handleDateParse(DateTimeParseException e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(), "日期格式错误，应为 yyyy-MM-dd");
    }

    /**
     * 业务校验不通过（如计划/训练项不存在、日期范围非法）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("业务参数错误: {}", e.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    /**
     * 兜底：未预期异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.ERROR);
    }
}
