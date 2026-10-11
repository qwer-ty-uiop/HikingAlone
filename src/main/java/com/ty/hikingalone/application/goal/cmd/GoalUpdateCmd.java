package com.ty.hikingalone.application.goal.cmd;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑目标命令（只许改 target/status）
 */
@Data
public class GoalUpdateCmd {

    /** 目标 id */
    private Long id;

    /** 目标值（可空=不更新） */
    private BigDecimal targetValue;

    /** 状态 0 已放弃 / 1 进行中（可空=不更新） */
    private Integer status;
}
