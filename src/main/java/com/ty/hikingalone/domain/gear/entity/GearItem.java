package com.ty.hikingalone.domain.gear.entity;

import com.ty.hikingalone.common.enums.gear.GearCategoryEnum;
import com.ty.hikingalone.common.enums.gear.GearItemStatusEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 装备台账领域实体（聚合根）
 * <p>退役（status=0）是台账的正常生命周期而非删除，保留与历史清单条目的对应关系</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GearItem {

    /** 名称长度上限 */
    public static final int MAX_NAME_LENGTH = 100;

    /** 自重上限 克 */
    public static final int MAX_WEIGHT_GRAMS = 30000;

    /** 品牌长度上限 */
    public static final int MAX_BRAND_LENGTH = 50;

    /** 备注长度上限 */
    public static final int MAX_NOTE_LENGTH = 200;

    private Long id;

    private Long userId;

    private String name;

    /**
     * 品类（GearCategoryEnum.code）
     */
    private Integer category;

    /**
     * 自重 克（可空）
     */
    private Integer weightGrams;

    /**
     * 品牌（可空，trim 后落库）
     */
    private String brand;

    /**
     * 备注（可空，trim 后落库）
     */
    private String note;

    /**
     * 状态（GearItemStatusEnum.code：0 已退役 / 1 在役）
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：录入装备，默认在役
     */
    public static GearItem create(Long userId, String name, Integer category, Integer weightGrams,
                                  String brand, String note) {
        GearItem item = new GearItem();
        item.userId = userId;
        item.createTime = LocalDateTime.now();
        item.applyContent(name, category, weightGrams, brand, note);
        item.status = GearItemStatusEnum.IN_SERVICE.getCode();
        return item;
    }

    /**
     * 编辑装备：全量替换台账内容（含状态），刷新 updateTime
     */
    public void update(String name, Integer category, Integer weightGrams, String brand, String note, Integer status) {
        GearItemStatusEnum.fromCode(status);
        applyContent(name, category, weightGrams, brand, note);
        this.status = status;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 台账内容组装与校验
     */
    private void applyContent(String name, Integer category, Integer weightGrams, String brand, String note) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("装备名不能为空");
        }
        String n = name.trim();
        if (n.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("装备名不能超过 100 字");
        }
        GearCategoryEnum.fromCode(category);
        if (weightGrams != null && (weightGrams < 0 || weightGrams > MAX_WEIGHT_GRAMS)) {
            throw new IllegalArgumentException("自重需在 0 ~ 30000 克之间");
        }
        if (brand != null && brand.trim().length() > MAX_BRAND_LENGTH) {
            throw new IllegalArgumentException("品牌不能超过 50 字");
        }
        if (note != null && note.trim().length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("备注不能超过 200 字");
        }
        this.name = n;
        this.category = category;
        this.weightGrams = weightGrams;
        this.brand = brand == null || brand.isBlank() ? null : brand.trim();
        this.note = note == null || note.isBlank() ? null : note.trim();
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static GearItem reconstruct(Long id, Long userId, String name, Integer category, Integer weightGrams,
                                       String brand, String note, Integer status,
                                       LocalDateTime createTime, LocalDateTime updateTime) {
        GearItem item = new GearItem();
        item.id = id;
        item.userId = userId;
        item.name = name;
        item.category = category;
        item.weightGrams = weightGrams;
        item.brand = brand;
        item.note = note;
        item.status = status;
        item.createTime = createTime;
        item.updateTime = updateTime;
        return item;
    }
}
