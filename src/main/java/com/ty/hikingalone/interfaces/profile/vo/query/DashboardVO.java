package com.ty.hikingalone.interfaces.profile.vo.query;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 个人中心驾驶舱 VO（GET /profile/dashboard）：零表读时聚合
 */
@Data
public class DashboardVO {

    /** 今日打卡状态 */
    private Today today;

    /** 待办提醒（规则引擎实时计算，可为空数组） */
    private List<Todo> todos;

    /** 徒步概览 */
    private Hiking hiking;

    /** 连续记录 */
    private Streaks streaks;

    /** 最近日记（≤3 篇） */
    private List<JournalItem> recentJournals;

    /** 进行中目标（≤4 个，含读时进度） */
    private List<GoalItem> activeGoals;

    /** 心愿概览 */
    private Wish wish;

    /** 徽章概览 */
    private Achievement achievement;

    /** 今日打卡（来自 daily_checkin，未打卡时各值为 null） */
    @Data
    public static class Today {

        private Integer waterCups;

        private Integer steps;

        private BigDecimal sleepHours;

        private Integer sleepQuality;

        /** 今天是否已打卡（任意字段有值即算） */
        private Boolean checkedIn;
    }

    /** 待办提醒 */
    @Data
    public static class Todo {

        /** 规则类型：JOURNAL_PENDING 轨迹未写日记 / GOAL_BEHIND 目标进度落后 */
        private String type;

        /** 提醒文案 */
        private String message;

        /** 前端跳转目标路由 */
        private String target;
    }

    /** 徒步概览 */
    @Data
    public static class Hiking {

        private Integer totalCount;

        private BigDecimal totalDistanceKm;

        private BigDecimal totalElevationGain;

        private Integer thisYearCount;

        private BigDecimal thisYearDistanceKm;
    }

    /** 连续记录 */
    @Data
    public static class Streaks {

        /** 最近连续有徒步日记的月份数 */
        private Integer journalStreakMonths;

        /** 身体数据连续记录天数 */
        private Integer bodyStreakDays;
    }

    /** 最近日记条目 */
    @Data
    public static class JournalItem {

        private Long id;

        private String title;

        private String hikeDate;

        private BigDecimal distanceKm;
    }

    /** 进行中目标条目 */
    @Data
    public static class GoalItem {

        private Long id;

        private String metricLabel;

        private BigDecimal targetValue;

        private BigDecimal current;

        private Integer percent;

        private String unit;
    }

    /** 心愿概览 */
    @Data
    public static class Wish {

        private Integer wishCount;

        private Integer plannedCount;

        private Integer doneCount;
    }

    /** 徽章概览 */
    @Data
    public static class Achievement {

        private Integer achievedCount;

        private Integer totalCount;
    }
}
