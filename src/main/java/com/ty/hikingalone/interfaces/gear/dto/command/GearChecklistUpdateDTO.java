package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑打包清单请求体：非 null 字段覆盖
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GearChecklistUpdateDTO extends GearChecklistSaveDTO {

    /**
     * 清单 id
     */
    @NotNull(message = "清单 id 不能为空")
    private Long id;

    /**
     * 状态 0 准备中 / 1 已打包 / 2 已结束（可空=不更新）
     */
    private Integer status;
}
