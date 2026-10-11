package com.ty.hikingalone.interfaces.body.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条身体数据记录 VO
 */
@Data
public class BodyMetricRecordVO {

    private Long id;

    /** 记录日期 yyyy-MM-dd */
    private String date;

    /** 体重 kg（1 位小数） */
    private BigDecimal weightKg;

    /** 晨起静息心率 次/分（可空） */
    private Integer restingHeartRate;

    /** 备注（可空） */
    private String note;

    /** 最近修改时间（yyyy-MM-dd'T'HH:mm:ss，未修改过为 null） */
    private String updateTime;
}
