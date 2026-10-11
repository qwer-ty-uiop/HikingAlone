package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑装备请求体：全量替换（状态必填）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GearItemUpdateDTO extends GearItemSaveDTO {

    /**
     * 装备 id
     */
    @NotNull(message = "装备 id 不能为空")
    private Long id;

    /**
     * 状态 0 已退役 / 1 在役
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
