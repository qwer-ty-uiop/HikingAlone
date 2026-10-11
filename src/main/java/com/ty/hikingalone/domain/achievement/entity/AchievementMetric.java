package com.ty.hikingalone.domain.achievement.entity;

/**
 * 成就徽章统计口径枚举：目录按此取数，服务端负责把各口径算成 current 值
 */
public enum AchievementMetric {

    /** 累计徒步次数 */
    HIKE_COUNT,

    /** 累计里程 km */
    TOTAL_DISTANCE_KM,

    /** 累计爬升 m */
    TOTAL_ELEVATION_M,

    /** 单日最远 km */
    MAX_SINGLE_DISTANCE_KM,

    /** 累计训练打卡次数 */
    TRAIN_CHECKIN_COUNT
}
