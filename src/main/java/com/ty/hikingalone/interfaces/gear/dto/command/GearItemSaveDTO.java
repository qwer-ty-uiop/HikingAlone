package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新建装备请求体
 */
@Data
public class GearItemSaveDTO {

    /**
     * 装备名（1~100 字）
     */
    @NotBlank(message = "装备名不能为空")
    private String name;

    /**
     * 品类（1~11）
     */
    @NotNull(message = "品类不能为空")
    private Integer category;

    /**
     * 自重 克（可空，[0,30000]）
     */
    private Integer weightGrams;

    /**
     * 品牌（可空，≤50 字）
     */
    private String brand;

    /**
     * 备注（可空，≤200 字）
     */
    private String note;
}
