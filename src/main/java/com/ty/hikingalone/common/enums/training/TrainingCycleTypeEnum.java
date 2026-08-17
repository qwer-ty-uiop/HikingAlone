package com.ty.hikingalone.common.enums.training;

import lombok.Getter;

/**
 * 训练计划周期类型枚举
 * <p>0 不重复（默认，等同老行为）；1 每天；2 每周；3 每月；4 每年</p>
 */
@Getter
public enum TrainingCycleTypeEnum {

    NONE(0, "不重复"),
    DAILY(1, "每天"),
    WEEKLY(2, "每周"),
    MONTHLY(3, "每月"),
    YEARLY(4, "每年");

    private final Integer code;
    private final String desc;

    TrainingCycleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 按 code 反查枚举；未知 code 抛 IllegalArgumentException
     */
    public static TrainingCycleTypeEnum fromCode(Integer code) {
        for (TrainingCycleTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("周期类型不合法");
    }
}
