package com.ty.hikingalone.interfaces.daily.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日打卡汇总统计 VO（应用服务跨记录计算）
 */
@Data
public class DailySummaryVO {

    /** 查询区间内有任何记录的天数 */
    private Integer recordedDays;

    /** 近 7 天饮水均值 杯/天（无值为 null） */
    private BigDecimal avgWater7;

    /** 近 7 天步数均值 步/天（无值为 null） */
    private BigDecimal avgSteps7;

    /** 近 7 天睡眠均值 小时/天（无值为 null） */
    private BigDecimal avgSleep7;
}
