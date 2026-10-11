package com.ty.hikingalone.domain.gear.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 常用装备目录条目（平台级共享只读数据，无 user_id，区别于用户装备库）
 * <p>供前端输入名称时即时联想：名称 + 品类 + 真实克重，免去手动填表</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GearCatalogItem {

    private Long id;

    private String name;

    /**
     * 品类（GearCategoryEnum.code）
     */
    private Integer category;

    /**
     * 典型自重 克（可空）
     */
    private Integer weightGrams;

    private Integer sort;

    /**
     * 仓储重建：从持久化层还原（目录数据只读，无业务行为）
     */
    public static GearCatalogItem reconstruct(Long id, String name, Integer category,
                                              Integer weightGrams, Integer sort) {
        GearCatalogItem item = new GearCatalogItem();
        item.id = id;
        item.name = name;
        item.category = category;
        item.weightGrams = weightGrams;
        item.sort = sort;
        return item;
    }
}
