package com.ty.hikingalone.common.enums.gear;

import lombok.Getter;

/**
 * 打包清单状态枚举
 */
@Getter
public enum GearChecklistStatusEnum {

    PREPARING(0, "准备中"),
    PACKED(1, "已打包"),
    FINISHED(2, "已结束");

    private final Integer code;

    private final String desc;

    GearChecklistStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static GearChecklistStatusEnum fromCode(Integer code) {
        for (GearChecklistStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("清单状态不合法");
    }
}
