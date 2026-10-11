package com.ty.hikingalone.interfaces.daily.dto.command;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日打卡请求体：全部字段可空（至少一项非 null，服务端校验），非 null 才覆盖
 */
@Data
public class DailyCheckinDTO {

    /**
     * 记录日期（yyyy-MM-dd，可空=今天；不允许未来日期）
     */
    private String date;

    /**
     * 饮水 杯（0~30）
     */
    private Integer waterCups;

    /**
     * 步数（0~200000）
     */
    private Integer steps;

    /**
     * 睡眠时长 小时（(0,24]）
     */
    private BigDecimal sleepHours;

    /**
     * 睡眠质量 1~5
     */
    private Integer sleepQuality;
}
