package com.ty.hikingalone.infrastructure.daily.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日打卡表 PO
 */
@Data
@TableName("daily_checkin")
public class DailyCheckinPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 饮水 杯（可空）
     */
    private Integer waterCups;

    /**
     * 步数（可空）
     */
    private Integer steps;

    /**
     * 睡眠时长 小时（可空）
     */
    private BigDecimal sleepHours;

    /**
     * 睡眠质量 1~5（可空）
     */
    private Integer sleepQuality;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
