package com.ty.hikingalone.common.context;

/**
 * 登录用户上下文：拦截器从 HttpSession 取出登录用户 id 存入 ThreadLocal，
 * 供 Controller / Service 同步获取（request 作用域内线程安全）。
 */
public class LoginUserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
