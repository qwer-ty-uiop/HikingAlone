package com.ty.hikingalone.common.enums.goal;

import lombok.Getter;

/**
 * 目标指标枚举：月度/年度 × 里程/次数/爬升
 */
@Getter
public enum GoalMetricEnum {

    MONTHLY_DISTANCE(1, "月徒步里程", GoalPeriodTypeEnum.MONTHLY, "km", new int[]{1, 5000}),
    MONTHLY_HIKES(2, "月徒步次数", GoalPeriodTypeEnum.MONTHLY, "次", new int[]{1, 365}),
    MONTHLY_ELEVATION(3, "月累计爬升", GoalPeriodTypeEnum.MONTHLY, "m", new int[]{1, 200000}),
    YEARLY_DISTANCE(4, "年徒步里程", GoalPeriodTypeEnum.YEARLY, "km", new int[]{1, 5000}),
    YEARLY_HIKES(5, "年徒步次数", GoalPeriodTypeEnum.YEARLY, "次", new int[]{1, 365}),
    YEARLY_ELEVATION(6, "年累计爬升", GoalPeriodTypeEnum.YEARLY, "m", new int[]{1, 200000});

    private final Integer code;

    private final String desc;

    private final GoalPeriodTypeEnum periodType;

    private final String unit;

    /** 目标值钳制区间 [min, max]（次数/爬升按整数语义，里程可 1 位小数） */
    private final int[] targetRange;

    GoalMetricEnum(Integer code, String desc, GoalPeriodTypeEnum periodType, String unit, int[] targetRange) {
        this.code = code;
        this.desc = desc;
        this.periodType = periodType;
        this.unit = unit;
        this.targetRange = targetRange;
    }

    /**
     * 按 code 解析，未知值抛参数异常
     */
    public static GoalMetricEnum fromCode(Integer code) {
        for (GoalMetricEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("目标指标不合法");
    }
}
