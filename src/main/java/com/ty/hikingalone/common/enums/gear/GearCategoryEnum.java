package com.ty.hikingalone.common.enums.gear;

import lombok.Getter;

/**
 * 装备品类枚举
 */
@Getter
public enum GearCategoryEnum {

    BACKPACK(1, "背包"),
    SHELTER(2, "帐篷天幕"),
    SLEEPING(3, "睡袋防潮垫"),
    FOOTWEAR(4, "鞋袜"),
    CLOTHING(5, "服装"),
    COOKING(6, "炊具饮水"),
    LIGHTING(7, "照明"),
    NAVIGATION(8, "导航"),
    SAFETY(9, "安全急救"),
    ELECTRONICS(10, "电子设备"),
    OTHER(11, "其他");

    private final Integer code;

    private final String desc;

    GearCategoryEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 解析，未知值抛参数异常（不变量校验入口）
     */
    public static GearCategoryEnum fromCode(Integer code) {
        for (GearCategoryEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("装备品类不合法");
    }
}
