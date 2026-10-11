package com.ty.hikingalone.interfaces.journal.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 徒步日记列表项 VO（正文截断为预览）
 */
@Data
public class JournalVO {

    private Long id;

    /** 标题 */
    private String title;

    /** 徒步日期 yyyy-MM-dd */
    private String hikeDate;

    /** 关联轨迹 id（可空） */
    private Long routeId;

    /** 关联轨迹名称（轨迹被删后为 null） */
    private String routeName;

    /** 里程 km（可空） */
    private BigDecimal distanceKm;

    /** 时长 分钟（可空） */
    private Integer durationMin;

    /** 累计爬升 m（可空） */
    private BigDecimal elevationGain;

    /** 难度 1~5 */
    private Integer difficulty;

    /** 心情 1~5 */
    private Integer mood;

    /** 天气描述（可空） */
    private String weather;

    /** 同伴（可空） */
    private String companions;

    /** 本次总花费（可空） */
    private BigDecimal cost;

    /** 正文预览（≤120 字；无正文为 null） */
    private String contentPreview;
}
