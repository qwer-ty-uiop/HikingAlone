package com.ty.hikingalone.interfaces.journal.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新建/编辑徒步日记请求体（编辑时 id 必填，见 /journal/update）
 */
@Data
public class JournalSaveDTO {

    /**
     * 标题（1~100 字）
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 徒步日期（yyyy-MM-dd，≤今天）
     */
    @NotBlank(message = "徒步日期不能为空")
    private String hikeDate;

    /**
     * 关联轨迹 id（可空；非空时校验归属并自动带出缺失指标）
     */
    private Long routeId;

    /**
     * 里程 km（可空，(0,500]）
     */
    private BigDecimal distanceKm;

    /**
     * 时长 分钟（可空，[1,4320]）
     */
    private Integer durationMin;

    /**
     * 累计爬升 m（可空，[0,10000]）
     */
    private BigDecimal elevationGain;

    /**
     * 难度 1~5
     */
    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    /**
     * 心情 1~5
     */
    @NotNull(message = "心情不能为空")
    private Integer mood;

    /**
     * 天气描述（可空，≤50 字）
     */
    private String weather;

    /**
     * 同伴（可空，≤100 字）
     */
    private String companions;

    /**
     * 本次总花费（可空，≥0）
     */
    private BigDecimal cost;

    /**
     * 正文（可空，≤8000 字）
     */
    private String content;
}
