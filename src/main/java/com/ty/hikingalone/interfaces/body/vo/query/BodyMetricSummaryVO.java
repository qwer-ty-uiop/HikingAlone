package com.ty.hikingalone.interfaces.body.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 身体数据汇总统计 VO（应用服务跨记录计算）
 */
@Data
public class BodyMetricSummaryVO {

    /** 最近一条记录的体重；无记录时 null */
    private BigDecimal latestWeight;

    /** 倒数第二条记录的体重；不足两条时 null */
    private BigDecimal prevWeight;

    /** latest - prev（1 位小数，负数=下降）；不足两条时 null */
    private BigDecimal weightDelta;

    /** 查询区间内有记录的天数 */
    private Integer recordedDays;

    /** 连续记录天数（今天未记录则从昨天起算） */
    private Integer streakDays;
}
