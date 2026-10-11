package com.ty.hikingalone.interfaces.gear.vo.query;

import com.ty.hikingalone.domain.gear.service.GearAdvisor;
import lombok.Data;

import java.util.List;

/**
 * AI 装备建议 VO（POST /gear/suggest）
 */
@Data
public class GearSuggestVO {

    /** 建议来源：LLM / RULE（LLM 未配置或失败时降级） */
    private String source;

    /** 模型标识（规则引擎为「内置规则引擎」） */
    private String model;

    /** 建议装备列表（≤20 条） */
    private List<Item> items;

    /** 装包建议文案 */
    private List<String> packingTips;

    @Data
    public static class Item {

        /** 装备名 */
        private String name;

        /** 品类 1~11（对齐装备库枚举） */
        private Integer category;

        /** 建议克重（可空） */
        private Integer weightGrams;

        /** 1 必带 / 2 建议 / 3 可选 */
        private Integer priority;

        /** 一句话理由 */
        private String reason;
    }

    /**
     * 领域建议值对象 → VO
     */
    public static GearSuggestVO of(GearAdvisor.GearAdvice advice) {
        GearSuggestVO vo = new GearSuggestVO();
        vo.setSource(advice.source());
        vo.setModel(advice.model());
        vo.setItems(advice.items().stream().map(i -> {
            Item item = new Item();
            item.setName(i.name());
            item.setCategory(i.category());
            item.setWeightGrams(i.weightGrams());
            item.setPriority(i.priority());
            item.setReason(i.reason());
            return item;
        }).toList());
        vo.setPackingTips(advice.packingTips());
        return vo;
    }
}
