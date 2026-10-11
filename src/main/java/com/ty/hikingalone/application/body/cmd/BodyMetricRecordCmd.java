package com.ty.hikingalone.application.body.cmd;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 记录/更新当日身体数据命令
 * <p>date 为 yyyy-MM-dd 文本（可空=今天），由应用服务解析并校验格式/未来日期</p>
 */
@Data
public class BodyMetricRecordCmd {

    /** 记录日期（yyyy-MM-dd，可空=今天） */
    private String date;

    /** 体重 kg（必填，25.0~250.0） */
    private BigDecimal weightKg;

    /** 晨起静息心率 次/分（可空，30~220） */
    private Integer restingHeartRate;

    /** 备注（可空，≤200 字） */
    private String note;
}
