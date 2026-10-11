package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 清单加条目请求体
 */
@Data
public class ChecklistItemAddDTO {

    /**
     * 所属清单 id
     */
    @NotNull(message = "清单 id 不能为空")
    private Long checklistId;

    /**
     * 来源装备 id（可空，仅标记来源）
     */
    private Long gearItemId;

    /**
     * 条目名（1~100 字）
     */
    @NotBlank(message = "条目名不能为空")
    private String name;

    /**
     * 克重快照（可空，[0,30000]）
     */
    private Integer weightGrams;
}
