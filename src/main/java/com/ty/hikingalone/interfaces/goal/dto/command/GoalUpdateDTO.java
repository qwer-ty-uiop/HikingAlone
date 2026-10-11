package com.ty.hikingalone.interfaces.goal.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑目标请求体（只许改 target/status）
 */
@Data
public class GoalUpdateDTO {

    /**
     * 目标 id
     */
    @NotNull(message = "目标 id 不能为空")
    private Long id;

    /**
     * 目标值（可空=不更新）
     */
    private BigDecimal targetValue;

    /**
     * 状态 0 已放弃 / 1 进行中（可空=不更新）
     */
    private Integer status;
}
