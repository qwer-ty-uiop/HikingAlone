package com.ty.hikingalone.common.enums.goal;

import lombok.Getter;

/**
 * 目标周期类型枚举
 */
@Getter
public enum GoalPeriodTypeEnum {

    MONTHLY(1, "月度"),
    YEARLY(2, "年度");

    private final Integer code;

    private final String desc;

    GoalPeriodTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
