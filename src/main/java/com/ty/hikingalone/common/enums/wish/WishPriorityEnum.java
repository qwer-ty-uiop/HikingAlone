package com.ty.hikingalone.common.enums.wish;

import lombok.Getter;

/**
 * 心愿优先级枚举
 */
@Getter
public enum WishPriorityEnum {

    CASUAL(1, "随缘"),
    WANT(2, "想去"),
    MUST(3, "一定要去");

    private final Integer code;

    private final String desc;

    WishPriorityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static WishPriorityEnum fromCode(Integer code) {
        for (WishPriorityEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("优先级不合法");
    }
}
