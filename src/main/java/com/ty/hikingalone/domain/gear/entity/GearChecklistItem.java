package com.ty.hikingalone.domain.gear.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 打包清单条目（清单聚合的内部实体，离开清单无意义）
 * <p>对装备库是快照式弱引用：名称与克重在添加时落在条目上，之后装备库改名/退役不影响已有清单</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GearChecklistItem {

    /** 条目名长度上限 */
    public static final int MAX_NAME_LENGTH = 100;

    /** 克重快照上限 克 */
    public static final int MAX_WEIGHT_GRAMS = 30000;

    /** 单清单条目数上限 */
    public static final int MAX_ITEMS_PER_CHECKLIST = 100;

    private Long id;

    private Long checklistId;

    /**
     * 来源装备 id（弱引用，仅标记来源，可空）
     */
    private Long gearItemId;

    /**
     * 条目名（添加时快照）
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

    /**
     * 装载仓位 0随身 1顶袋 2背部中段 3上部 4包底（null = 按品类推荐归位）
     */
    private Integer slot;

    private LocalDateTime createTime;

    /**
     * 工厂方法：向清单添加条目（sort 由清单聚合分配）
     */
    public static GearChecklistItem create(Long checklistId, Long gearItemId, String name,
                                           Integer weightGrams, int sort) {
        GearChecklistItem item = new GearChecklistItem();
        item.checklistId = checklistId;
        item.gearItemId = gearItemId;
        item.createTime = LocalDateTime.now();
        item.checked = 0;
        item.applyContent(name, weightGrams);
        item.sort = sort;
        return item;
    }

    /**
     * 部分更新：只覆盖非 null 字段（勾选/改名/改克重/换仓位）
     */
    public void merge(Integer checked, String name, Integer weightGrams, Integer slot) {
        if (slot != null && (slot < 0 || slot > 4)) {
            throw new IllegalArgumentException("装载仓位不合法");
        }
        if (checked != null) {
            if (checked != 0 && checked != 1) {
                throw new IllegalArgumentException("打包状态只能是 0 或 1");
            }
            this.checked = checked;
        }
        if (name != null || weightGrams != null) {
            applyContent(name == null ? this.name : name,
                    weightGrams == null ? this.weightGrams : weightGrams);
        }
        if (slot != null) {
            this.slot = slot;
        }
    }

    /**
     * 条目内容校验（名称必填、克重区间）
     */
    private void applyContent(String name, Integer weightGrams) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("条目名不能为空");
        }
        String n = name.trim();
        if (n.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("条目名不能超过 100 字");
        }
        if (weightGrams != null && (weightGrams < 0 || weightGrams > MAX_WEIGHT_GRAMS)) {
            throw new IllegalArgumentException("克重需在 0 ~ 30000 克之间");
        }
        this.name = n;
        this.weightGrams = weightGrams;
    }

    /**
     * 仓储重建：从持久化层还原（不做业务校验）
     */
    public static GearChecklistItem reconstruct(Long id, Long checklistId, Long gearItemId, String name,
                                                Integer weightGrams, Integer checked, Integer sort, Integer slot,
                                                LocalDateTime createTime) {
        GearChecklistItem item = new GearChecklistItem();
        item.id = id;
        item.checklistId = checklistId;
        item.gearItemId = gearItemId;
        item.name = name;
        item.weightGrams = weightGrams;
        item.checked = checked;
        item.sort = sort;
        item.slot = slot;
        item.createTime = createTime;
        return item;
    }
}
