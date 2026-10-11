package com.ty.hikingalone.application.goal.cmd;

import lombok.Data;

/**
 * 目标按 id 操作命令
 */
@Data
public class GoalIdCmd {

    /** 目标 id */
    private Long id;
}
