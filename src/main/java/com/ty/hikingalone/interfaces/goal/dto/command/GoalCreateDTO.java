package com.ty.hikingalone.interfaces.goal.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新建目标请求体
 */
@Data
public class GoalCreateDTO {

    /**
     * 指标（1~6）
     */
    @NotNull(message = "指标不能为空")
    private Integer metric;

    /**
     * 目标值（按 metric 钳制）
     */
    @NotNull(message = "目标值不能为空")
    private BigDecimal targetValue;

    /**
     * 年份 [2020,2100]
     */
    @NotNull(message = "年份不能为空")
    private Integer year;

    /**
     * 月 [1,12]（仅月度目标必填）
     */
    private Integer month;
}
