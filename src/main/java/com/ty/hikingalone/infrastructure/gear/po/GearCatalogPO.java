package com.ty.hikingalone.infrastructure.gear.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 常用装备目录表 PO（平台级共享只读数据）
 */
@Data
@TableName("gear_catalog")
public class GearCatalogPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 品类 1~11
     */
    private Integer category;

    /**
     * 典型自重 克（可空）
     */
    private Integer weightGrams;

    private Integer sort;
}
