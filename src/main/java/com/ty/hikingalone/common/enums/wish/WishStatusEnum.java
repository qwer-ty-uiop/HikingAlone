package com.ty.hikingalone.common.enums.wish;

import lombok.Getter;

/**
 * 心愿状态枚举（0 想去 / 1 已计划 → 2 已完成 / 3 已放弃；终态不可回退）
 */
@Getter
public enum WishStatusEnum {

    WISH(0, "想去"),
    PLANNED(1, "已计划"),
    DONE(2, "已完成"),
    GIVEN_UP(3, "已放弃");

    private final Integer code;

    private final String desc;

    WishStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static WishStatusEnum fromCode(Integer code) {
        for (WishStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("心愿状态不合法");
    }
}
