package com.ty.hikingalone.domain.achievement;

import com.ty.hikingalone.domain.achievement.entity.AchievementDefinition;
import com.ty.hikingalone.domain.achievement.entity.AchievementMetric;

import java.math.BigDecimal;
import java.util.List;

/**
 * 徽章目录（v1，14 枚 / 4 组）：新增徽章 = 加一条定义，无需数据迁移
 */
public final class AchievementCatalog {

    private static final List<AchievementDefinition> DEFINITIONS = List.of(
            // 步履（徒步次数）
            new AchievementDefinition("hike_count_1", "步履", "初次出发", "完成第一次徒步",
                    BigDecimal.ONE, AchievementMetric.HIKE_COUNT),
            new AchievementDefinition("hike_count_10", "步履", "渐入佳境", "累计徒步 10 次",
                    BigDecimal.TEN, AchievementMetric.HIKE_COUNT),
            new AchievementDefinition("hike_count_50", "步履", "山野常客", "累计徒步 50 次",
                    BigDecimal.valueOf(50), AchievementMetric.HIKE_COUNT),
            // 里程（累计公里）
            new AchievementDefinition("distance_10", "里程", "迈出第一步", "累计里程 10 km",
                    BigDecimal.TEN, AchievementMetric.TOTAL_DISTANCE_KM),
            new AchievementDefinition("distance_100", "里程", "百里行人", "累计里程 100 km",
                    BigDecimal.valueOf(100), AchievementMetric.TOTAL_DISTANCE_KM),
            new AchievementDefinition("distance_500", "里程", "千里江山", "累计里程 500 km",
                    BigDecimal.valueOf(500), AchievementMetric.TOTAL_DISTANCE_KM),
            // 爬升（累计米）
            new AchievementDefinition("elevation_500", "爬升", "越过小丘", "累计爬升 500 m",
                    BigDecimal.valueOf(500), AchievementMetric.TOTAL_ELEVATION_M),
            new AchievementDefinition("elevation_5000", "爬升", "层峦叠嶂", "累计爬升 5000 m",
                    BigDecimal.valueOf(5000), AchievementMetric.TOTAL_ELEVATION_M),
            new AchievementDefinition("elevation_20000", "爬升", "云海之巅", "累计爬升 20000 m",
                    BigDecimal.valueOf(20000), AchievementMetric.TOTAL_ELEVATION_M),
            // 单日（一次徒步的最远距离）
            new AchievementDefinition("single_21_1", "单日", "半程马拉松", "单日徒步 21.1 km",
                    new BigDecimal("21.1"), AchievementMetric.MAX_SINGLE_DISTANCE_KM),
            new AchievementDefinition("single_42_2", "单日", "全程马拉松", "单日徒步 42.2 km",
                    new BigDecimal("42.2"), AchievementMetric.MAX_SINGLE_DISTANCE_KM),
            // 自律（训练打卡次数）
            new AchievementDefinition("train_10", "自律", "热身完毕", "累计训练打卡 10 次",
                    BigDecimal.TEN, AchievementMetric.TRAIN_CHECKIN_COUNT),
            new AchievementDefinition("train_100", "自律", "自律成瘾", "累计训练打卡 100 次",
                    BigDecimal.valueOf(100), AchievementMetric.TRAIN_CHECKIN_COUNT),
            new AchievementDefinition("train_500", "自律", "钢铁意志", "累计训练打卡 500 次",
                    BigDecimal.valueOf(500), AchievementMetric.TRAIN_CHECKIN_COUNT)
    );

    private AchievementCatalog() {
    }

    /**
     * 全部徽章定义（目录顺序即展示顺序，同分组连续）
     */
    public static List<AchievementDefinition> all() {
        return DEFINITIONS;
    }
}
