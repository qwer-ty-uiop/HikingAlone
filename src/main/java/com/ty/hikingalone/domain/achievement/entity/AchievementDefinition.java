package com.ty.hikingalone.domain.achievement.entity;

import java.math.BigDecimal;

/**
 * 徽章定义值对象（目录即代码：分组、名称、阈值、取数口径）
 *
 * @param code        徽章唯一编码
 * @param group       分组名（步履/里程/爬升/单日/自律）
 * @param name        徽章名
 * @param description 达成条件描述
 * @param target      阈值（恒 > 0）
 * @param metric      取数口径
 */
public record AchievementDefinition(String code, String group, String name, String description,
                                    BigDecimal target, AchievementMetric metric) {
}
