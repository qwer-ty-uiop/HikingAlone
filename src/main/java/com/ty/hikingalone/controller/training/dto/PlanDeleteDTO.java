package com.ty.hikingalone.controller.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 物理删除训练计划请求体
 */
@Data
public class PlanDeleteDTO {

    @NotNull(message = "计划id不能为空")
    private Long id;
}
