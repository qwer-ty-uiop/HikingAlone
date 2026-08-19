package com.ty.hikingalone.interfaces.training.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 软删除（放弃）训练计划请求体
 */
@Data
public class PlanAbandonDTO {

    @NotNull(message = "计划id不能为空")
    private Long id;
}
