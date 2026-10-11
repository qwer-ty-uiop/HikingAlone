package com.ty.hikingalone.interfaces.route.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 常走路线 VO（GET /routes/hot）：按徒步日记引用次数聚合
 */
@Data
public class RouteHotVO {

    /** 轨迹 id */
    private Long routeId;

    /** 路线名 */
    private String name;

    /** 里程 km */
    private BigDecimal distanceKm;

    /** 时长 分钟 */
    private Integer durationMin;

    /** 累计爬升 m */
    private BigDecimal elevationGain;

    /** 走过次数（日记引用数） */
    private Integer hikeCount;

    /** 最近一次徒步日期 yyyy-MM-dd */
    private String lastHikeDate;

    /** 最早一次徒步日期 yyyy-MM-dd */
    private String firstHikeDate;
}
