package com.ty.hikingalone.common.enums.gear;

import lombok.Getter;

/**
 * 装备在役状态枚举（退役不删，保留台账与历史清单对应关系）
 */
@Getter
public enum GearItemStatusEnum {

    RETIRED(0, "已退役"),
    IN_SERVICE(1, "在役");

    private final Integer code;

    private final String desc;

    GearItemStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static GearItemStatusEnum fromCode(Integer code) {
        for (GearItemStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("装备状态不合法");
    }
}
