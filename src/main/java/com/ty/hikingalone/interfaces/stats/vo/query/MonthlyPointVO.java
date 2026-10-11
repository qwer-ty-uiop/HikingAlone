package com.ty.hikingalone.interfaces.stats.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度分布点 VO
 */
@Data
public class MonthlyPointVO {

    /** 月 1~12 */
    private Integer month;

    /** 当月徒步次数 */
    private Integer hikeCount;

    /** 当月里程 km */
    private BigDecimal distanceKm;
}
