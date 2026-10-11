package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 装备/清单/条目按 id 操作请求体（删除、复制等）
 */
@Data
public class GearIdDTO {

    /**
     * 目标 id
     */
    @NotNull(message = "id 不能为空")
    private Long id;
}
