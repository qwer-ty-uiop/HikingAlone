package com.ty.hikingalone.infrastructure.body.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日身体数据表 PO
 */
@Data
@TableName("body_metric")
public class BodyMetricPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 体重 kg（25.0~250.0）
     */
    private BigDecimal weightKg;

    /**
     * 晨起静息心率 次/分（可空）
     */
    private Integer restingHeartRate;

    /**
     * 备注（可空）
     */
    private String note;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
