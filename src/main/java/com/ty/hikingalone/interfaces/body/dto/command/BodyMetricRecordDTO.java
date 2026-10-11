package com.ty.hikingalone.interfaces.body.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记录/更新当日身体数据请求体
 */
@Data
public class BodyMetricRecordDTO {

    /**
     * 记录日期（yyyy-MM-dd，可空=今天；不允许未来日期）
     */
    private String date;

    /**
     * 体重 kg（必填，25.0~250.0）
     */
    @NotNull(message = "体重不能为空")
    private BigDecimal weightKg;

    /**
     * 晨起静息心率 次/分（可空，30~220）
     */
    private Integer restingHeartRate;

    /**
     * 备注（可空，≤200 字）
     */
    private String note;
}
