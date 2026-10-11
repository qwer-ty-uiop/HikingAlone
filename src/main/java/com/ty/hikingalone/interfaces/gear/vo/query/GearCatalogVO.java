package com.ty.hikingalone.interfaces.gear.vo.query;

import com.ty.hikingalone.domain.gear.entity.GearCatalogItem;
import lombok.Data;

/**
 * 常用装备目录条目 VO（GET /gear/catalog 联想数据）
 */
@Data
public class GearCatalogVO {

    private Long id;

    /** 通用装备名 */
    private String name;

    /** 品类 1~11 */
    private Integer category;

    /** 品牌（型号级条目，可空） */
    private String brand;

    /** 联想别名（可空） */
    private String searchTerms;

    /** 典型自重 克（可空） */
    private Integer weightGrams;

    /**
     * 领域目录条目 → VO
     */
    public static GearCatalogVO of(GearCatalogItem item) {
        GearCatalogVO vo = new GearCatalogVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setCategory(item.getCategory());
        vo.setBrand(item.getBrand());
        vo.setSearchTerms(item.getSearchTerms());
        vo.setWeightGrams(item.getWeightGrams());
        return vo;
    }
}
