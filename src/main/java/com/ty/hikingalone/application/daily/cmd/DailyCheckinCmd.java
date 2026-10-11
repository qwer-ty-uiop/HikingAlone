package com.ty.hikingalone.application.daily.cmd;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日打卡命令：全部字段可空，非 null 才覆盖（部分更新语义）
 * <p>date 为 yyyy-MM-dd 文本（可空=今天），由应用服务解析</p>
 */
@Data
public class DailyCheckinCmd {

    /** 记录日期（yyyy-MM-dd，可空=今天） */
    private String date;

    /** 饮水 杯（0~30，非 null 才覆盖） */
    private Integer waterCups;

    /** 步数（0~200000，非 null 才覆盖） */
    private Integer steps;

    /** 睡眠时长 小时（(0,24]，非 null 才覆盖） */
    private BigDecimal sleepHours;

    /** 睡眠质量 1~5（非 null 才覆盖） */
    private Integer sleepQuality;
}
