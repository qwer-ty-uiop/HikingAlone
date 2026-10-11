package com.ty.hikingalone.application.gear.cmd;

import lombok.Data;

/**
 * 新建/编辑装备命令（编辑时 id 与 status 必填）
 */
@Data
public class GearItemCmd {

    /** 装备 id（编辑必填） */
    private Long id;

    /** 装备名（1~100 字） */
    private String name;

    /** 品类（GearCategoryEnum.code，1~11） */
    private Integer category;

    /** 自重 克（可空，[0,30000]） */
    private Integer weightGrams;

    /** 品牌（可空，≤50 字） */
    private String brand;

    /** 备注（可空，≤200 字） */
    private String note;

    /** 状态（GearItemStatusEnum.code，编辑必填） */
    private Integer status;
}
