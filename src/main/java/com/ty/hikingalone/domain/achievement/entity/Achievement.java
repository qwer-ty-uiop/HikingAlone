package com.ty.hikingalone.domain.achievement.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 徽章实例值对象：定义 + 当前口径值 + 达成状态（由目录定义与实时数据计算而来）
 */
public record Achievement(String code, String group, String name, String description,
                          BigDecimal target, BigDecimal current, boolean achieved, int progressPercent) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * 由定义与当前值装配：achieved = current >= target；progressPercent = min(100, floor(current/target*100))
     */
    public static Achievement of(AchievementDefinition def, BigDecimal current) {
        BigDecimal safeCurrent = current == null ? BigDecimal.ZERO : current;
        boolean achieved = safeCurrent.compareTo(def.target()) >= 0;
        int percent = def.target().compareTo(BigDecimal.ZERO) <= 0 ? 0
                : Math.min(100, safeCurrent.multiply(HUNDRED)
                        .divide(def.target(), 0, RoundingMode.DOWN)
                        .intValue());
        return new Achievement(def.code(), def.group(), def.name(), def.description(),
                def.target(), safeCurrent, achieved, Math.max(0, percent));
    }
}
