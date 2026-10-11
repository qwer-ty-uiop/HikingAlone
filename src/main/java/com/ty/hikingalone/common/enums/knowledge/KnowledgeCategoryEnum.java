package com.ty.hikingalone.common.enums.knowledge;

import lombok.Getter;

/**
 * 知识笔记分类枚举
 */
@Getter
public enum KnowledgeCategoryEnum {

    SAFETY(1, "安全守则"),
    FIRST_AID(2, "急救"),
    GEAR_SKILL(3, "装备技巧"),
    ROUTE_INFO(4, "线路情报"),
    LNT(5, "环保准则"),
    NUTRITION(6, "营养补给"),
    OTHER(7, "其他");

    private final Integer code;

    private final String desc;

    KnowledgeCategoryEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static KnowledgeCategoryEnum fromCode(Integer code) {
        for (KnowledgeCategoryEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("笔记分类不合法");
    }
}
