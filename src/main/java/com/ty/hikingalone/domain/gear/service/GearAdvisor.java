package com.ty.hikingalone.domain.gear.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装备建议领域服务接口（依赖倒置：应用层只依赖本接口，实现收敛在基础设施层 LLM 适配器）
 */
public interface GearAdvisor {

    /**
     * 是否可用（未配置 LLM 凭证时返回 false，应用服务自行降级规则引擎）
     */
    boolean available();

    /**
     * 按行程上下文生成装备建议
     *
     * @return 建议结果；LLM 调用失败/输出不可解析时返回 empty（由应用服务降级），不抛出网络异常
     */
    java.util.Optional<GearAdvice> advise(GearAdviceContext context);

    /**
     * 行程上下文值对象：路线信息（可空）+ 行程参数 + 用户现有装备名（供 LLM 去重）
     */
    record GearAdviceContext(
            String routeName,
            BigDecimal distanceKm,
            BigDecimal elevationGainM,
            Integer durationMin,
            int days,
            int month,
            String extra,
            List<String> ownedItemNames) {
    }

    /**
     * 建议结果值对象
     *
     * @param source      来源标识（LLM / RULE）
     * @param model       模型标识（规则引擎为 RULE）
     * @param items       建议装备（name 必填，category 1~11，priority 1必带 2建议 3可选）
     * @param packingTips 装包建议文案
     */
    record GearAdvice(
            String source,
            String model,
            List<GearAdviceItem> items,
            List<String> packingTips) {
    }

    /** 单条建议 */
    record GearAdviceItem(String name, int category, Integer weightGrams, int priority, String reason) {
    }
}
