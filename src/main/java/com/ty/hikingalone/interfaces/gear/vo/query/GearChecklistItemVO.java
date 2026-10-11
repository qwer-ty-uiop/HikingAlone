package com.ty.hikingalone.interfaces.gear.vo.query;

import lombok.Data;

/**
 * 打包清单条目 VO
 */
@Data
public class GearChecklistItemVO {

    private Long id;

    /** 来源装备 id（弱引用，可空） */
    private Long gearItemId;

    /** 条目名（快照） */
    private String name;

    /** 克重快照（可空） */
    private Integer weightGrams;

    /** 0 未打包 / 1 已打包 */
    private Integer checked;

    /** 排序 */
    private Integer sort;

    /** 装载仓位 0随身 1顶袋 2背部中段 3上部 4包底（null=按品类推荐） */
    private Integer slot;
}
