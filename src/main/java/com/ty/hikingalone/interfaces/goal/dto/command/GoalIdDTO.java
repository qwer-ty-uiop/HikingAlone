package com.ty.hikingalone.interfaces.goal.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 目标按 id 操作请求体
 */
@Data
public class GoalIdDTO {

    /**
     * 目标 id
     */
    @NotNull(message = "目标 id 不能为空")
    private Long id;
}
