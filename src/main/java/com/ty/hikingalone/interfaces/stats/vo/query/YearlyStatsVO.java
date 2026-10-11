package com.ty.hikingalone.interfaces.stats.vo.query;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 年度统计 VO
 */
@Data
public class YearlyStatsVO {

    /** 年份 */
    private Integer year;

    /** 年度徒步次数 */
    private Integer hikeCount;

    /** 年度总里程 km */
    private BigDecimal totalDistanceKm;

    /** 年度总时长 分钟 */
    private Integer totalDurationMin;

    /** 年度总爬升 m */
    private BigDecimal totalElevationGain;

    /** 年度总花费 */
    private BigDecimal totalCost;

    /** 单日最远 km（无记录为 null） */
    private BigDecimal maxSingleDistanceKm;

    /** 单日最大爬升 m（无记录为 null） */
    private BigDecimal maxSingleElevationGain;

    /** 年度首次徒步日期 yyyy-MM-dd（无记录为 null） */
    private String firstHikeDate;

    /** 月度分布（固定 1~12 月，无徒步月补零） */
    private List<MonthlyPointVO> monthly;
}
