package com.ty.hikingalone.infrastructure.gear.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 装备台账表 PO
 */
@Data
@TableName("gear_item")
public class GearItemPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    /**
     * 品类 1~11
     */
    private Integer category;

    /**
     * 自重 克（可空）
     */
    private Integer weightGrams;

    /**
     * 品牌（可空）
     */
    private String brand;

    /**
     * 备注（可空）
     */
    private String note;

    /**
     * 0 已退役 / 1 在役
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
