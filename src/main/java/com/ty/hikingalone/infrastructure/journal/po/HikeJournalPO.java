package com.ty.hikingalone.infrastructure.journal.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徒步日记表 PO
 */
@Data
@TableName("hike_journal")
public class HikeJournalPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private LocalDate hikeDate;

    /**
     * 关联轨迹 id（可空）
     */
    private Long routeId;

    /**
     * 里程 km（可空）
     */
    private BigDecimal distanceKm;

    /**
     * 时长 分钟（可空）
     */
    private Integer durationMin;

    /**
     * 累计爬升 m（可空）
     */
    private BigDecimal elevationGain;

    /**
     * 难度 1~5
     */
    private Integer difficulty;

    /**
     * 心情 1~5
     */
    private Integer mood;

    /**
     * 天气描述（可空）
     */
    private String weather;

    /**
     * 同伴（可空）
     */
    private String companions;

    /**
     * 本次总花费（可空）
     */
    private BigDecimal cost;

    /**
     * 正文（可空）
     */
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
