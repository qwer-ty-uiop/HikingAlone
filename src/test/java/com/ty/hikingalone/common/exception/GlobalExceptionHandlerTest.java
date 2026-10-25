package com.ty.hikingalone.common.exception;

import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全局异常处理单测：不启动 Spring 上下文，直接调用 handler，
 * 断言 body code 与 HTTP 状态码一致（404/405 不再被兜底吞成 500/200）
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void noResourceFoundReturns404Not500() {
        Result<Void> result = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "/journal"), response);
        assertThat(result.getCode()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(result.getMessage()).isEqualTo("接口不存在");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void methodNotSupportedReturns405Not500() {
        Result<Void> result = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("GET"), response);
        assertThat(result.getCode()).isEqualTo(ResultCode.METHOD_NOT_ALLOWED.getCode());
        assertThat(response.getStatus()).isEqualTo(405);
    }

    @Test
    void dateParseReturns400() {
        Result<Void> result = handler.handleDateParse(new DateTimeParseException("bad", "2026-13-01", 0), response);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void illegalArgumentReturns400() {
        Result<Void> result = handler.handleIllegalArgument(new IllegalArgumentException("计划不存在"), response);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("计划不存在");
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void unexpectedExceptionStillReturns500() {
        Result<Void> result = handler.handleException(new RuntimeException("boom"), response);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(response.getStatus()).isEqualTo(500);
    }
}
