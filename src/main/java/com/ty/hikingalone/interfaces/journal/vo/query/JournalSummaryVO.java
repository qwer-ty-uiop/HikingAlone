package com.ty.hikingalone.interfaces.journal.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 徒步日记汇总统计 VO（应用服务跨记录计算）
 */
@Data
public class JournalSummaryVO {

    /** 累计徒步次数 */
    private Integer totalCount;

    /** 累计里程 km */
    private BigDecimal totalDistanceKm;

    /** 累计时长 分钟 */
    private Integer totalDurationMin;

    /** 累计爬升 m */
    private BigDecimal totalElevationGain;

    /** 累计花费 */
    private BigDecimal totalCost;

    /** 今年徒步次数 */
    private Integer thisYearCount;

    /** 今年里程 km */
    private BigDecimal thisYearDistanceKm;

    /** 单日最远 km（无记录为 null） */
    private BigDecimal maxSingleDistanceKm;

    /** 最近一次徒步日期 yyyy-MM-dd（无记录为 null） */
    private String lastHikeDate;
}
