package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 清单条目更新请求体：非 null 字段覆盖（勾选/改名/改克重）
 */
@Data
public class ChecklistItemUpdateDTO {

    /**
     * 条目 id
     */
    @NotNull(message = "条目 id 不能为空")
    private Long id;

    /**
     * 打包状态 0/1（可空=不更新）
     */
    private Integer checked;

    /**
     * 条目名（可空=不更新）
     */
    private String name;

    /**
     * 克重快照（可空=不更新）
     */
    private Integer weightGrams;
}
