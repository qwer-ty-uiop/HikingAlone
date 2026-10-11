package com.ty.hikingalone.interfaces.goal.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 目标 VO（含读时进度）
 */
@Data
public class GoalVO {

    private Long id;

    /** 指标 1~6 */
    private Integer metric;

    /** 指标文案（如「月徒步里程」） */
    private String metricLabel;

    /** 目标值 */
    private BigDecimal targetValue;

    /** 周期 1 月度 / 2 年度 */
    private Integer periodType;

    /** 年份 */
    private Integer year;

    /** 月 [1,12]；年度为 0 */
    private Integer month;

    /** 状态 0 已放弃 / 1 进行中 */
    private Integer status;

    /** 读时进度 */
    private Progress progress;

    /**
     * 进度子结构
     */
    @Data
    public static class Progress {

        /** 当前累计值 */
        private BigDecimal current;

        /** 完成百分比（钳制 [0,999]） */
        private Integer percent;

        /** 是否已达成 */
        private Boolean achieved;

        /** 周期是否已结束 */
        private Boolean periodEnded;

        /** 单位（km/次/m） */
        private String unit;
    }
}
