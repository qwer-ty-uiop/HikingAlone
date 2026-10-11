package com.ty.hikingalone.application.goal.cmd;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 新建目标命令（metric 决定周期类型；月度需 month）
 */
@Data
public class GoalCreateCmd {

    /** 指标（GoalMetricEnum.code，1~6） */
    private Integer metric;

    /** 目标值（按 metric 钳制） */
    private BigDecimal targetValue;

    /** 年份 [2020,2100] */
    private Integer year;

    /** 月 [1,12]（仅月度目标必填；年度可空） */
    private Integer month;
}
