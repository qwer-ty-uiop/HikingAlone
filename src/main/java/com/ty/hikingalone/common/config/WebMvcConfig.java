package com.ty.hikingalone.common.config;

import com.ty.hikingalone.common.interceptor.LoginUserInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册登录拦截器。
 * <p>拦截 /train/** 与 /user/logout（需登录），放行 /user/register、/user/login、/user/forget 及首页。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor)
                .addPathPatterns("/train/**", "/routes/**", "/user/me", "/user/logout")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/user/forget");
    }
}
