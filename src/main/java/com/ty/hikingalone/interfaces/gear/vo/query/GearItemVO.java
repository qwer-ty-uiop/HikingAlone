package com.ty.hikingalone.interfaces.gear.vo.query;

import lombok.Data;

/**
 * 装备台账 VO
 */
@Data
public class GearItemVO {

    private Long id;

    /** 装备名 */
    private String name;

    /** 品类 1~11 */
    private Integer category;

    /** 自重 克（可空） */
    private Integer weightGrams;

    /** 品牌（可空） */
    private String brand;

    /** 备注（可空） */
    private String note;

    /** 0 已退役 / 1 在役 */
    private Integer status;

    /** 创建时间（yyyy-MM-dd'T'HH:mm:ss） */
    private String createTime;
}
