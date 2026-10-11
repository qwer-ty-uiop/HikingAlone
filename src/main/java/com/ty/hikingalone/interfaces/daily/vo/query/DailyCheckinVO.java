package com.ty.hikingalone.interfaces.daily.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条每日打卡 VO
 */
@Data
public class DailyCheckinVO {

    private Long id;

    /** 记录日期 yyyy-MM-dd */
    private String date;

    /** 饮水 杯（可空） */
    private Integer waterCups;

    /** 步数（可空） */
    private Integer steps;

    /** 睡眠时长 小时（可空，1 位小数） */
    private BigDecimal sleepHours;

    /** 睡眠质量 1~5（可空） */
    private Integer sleepQuality;

    /** 最近修改时间（yyyy-MM-dd'T'HH:mm:ss，未修改过为 null） */
    private String updateTime;
}
