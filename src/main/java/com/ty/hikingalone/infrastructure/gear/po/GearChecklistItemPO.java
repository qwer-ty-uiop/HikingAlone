package com.ty.hikingalone.infrastructure.gear.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打包清单条目表 PO
 */
@Data
@TableName("gear_checklist_item")
public class GearChecklistItemPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checklistId;

    /**
     * 来源装备 id（弱引用，可空）
     */
    private Long gearItemId;

    /**
     * 条目名（快照）
     */
    private String name;

    /**
     * 克重快照（可空）
     */
    private Integer weightGrams;

    /**
     * 0 未打包 / 1 已打包
     */
    private Integer checked;

    /**
     * 排序
     */
    private Integer sort;

    private LocalDateTime createTime;
}
