package com.ty.hikingalone.common.enums;

import lombok.Getter;

/**
 * 通用启用状态枚举，对应各表 status 字段（0禁用 1启用）
 */
@Getter
public enum StatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
