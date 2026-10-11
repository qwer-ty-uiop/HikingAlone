package com.ty.hikingalone.domain.daily.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日打卡领域实体（聚合根）：饮水 / 步数 / 睡眠
 * <p>聚合边界 = 用户 × 日期，一天一行；与身体数据「整行覆盖」不同，三个指标在一天内
 * 各自独立发生（早起记睡眠、中午 +1 杯水、晚上记步数），因此打卡请求只覆盖非 null 字段。
 * 各指标区间不变量内聚在工厂与 merge 方法中</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyCheckin {

    /** 饮水上限 杯（250ml/杯） */
    public static final int MAX_WATER_CUPS = 30;

    /** 步数上限 */
    public static final int MAX_STEPS = 200000;

    /** 睡眠时长上限 小时（含） */
    public static final BigDecimal MAX_SLEEP_HOURS = new BigDecimal("24.0");

    /** 睡眠质量下限 */
    public static final int MIN_SLEEP_QUALITY = 1;

    /** 睡眠质量上限 */
    public static final int MAX_SLEEP_QUALITY = 5;

    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 饮水 杯（可空；0 表示刻意记录「今天没喝够」）
     */
    private Integer waterCups;

    /**
     * 步数（可空）
     */
    private Integer steps;

    /**
     * 睡眠时长 小时（可空，1 位小数）
     */
    private BigDecimal sleepHours;

    /**
     * 睡眠质量 1很差~5很好（可空）
     */
    private Integer sleepQuality;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：首次创建某天打卡（至少一个指标非 null 的前置判断在应用服务，聚合只保证取值合法）
     */
    public static DailyCheckin record(Long userId, LocalDate recordDate, Integer waterCups, Integer steps,
                                      BigDecimal sleepHours, Integer sleepQuality) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("记录日期不能为空");
        }
        if (recordDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("记录日期不能是未来");
        }
        DailyCheckin checkin = new DailyCheckin();
        checkin.userId = userId;
        checkin.recordDate = recordDate;
        checkin.createTime = LocalDateTime.now();
        checkin.applyMetrics(waterCups, steps, sleepHours, sleepQuality);
        return checkin;
    }

    /**
     * 部分覆盖：只更新非 null 字段，其余保持不变，刷新 updateTime。
     * <p>不改 userId/recordDate/createTime</p>
     */
    public void merge(Integer waterCups, Integer steps, BigDecimal sleepHours, Integer sleepQuality) {
        applyMetrics(waterCups, steps, sleepHours, sleepQuality);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 指标组装与校验：null 表示「本次不更新该字段」，非 null 需落在各自区间
     */
    private void applyMetrics(Integer waterCups, Integer steps, BigDecimal sleepHours, Integer sleepQuality) {
        if (waterCups != null && (waterCups < 0 || waterCups > MAX_WATER_CUPS)) {
            throw new IllegalArgumentException("饮水杯数需在 0 ~ 30 杯之间");
        }
        if (steps != null && (steps < 0 || steps > MAX_STEPS)) {
            throw new IllegalArgumentException("步数需在 0 ~ 200000 之间");
        }
        if (sleepHours != null) {
            BigDecimal h = sleepHours.setScale(1, RoundingMode.HALF_UP);
            if (h.compareTo(BigDecimal.ZERO) <= 0 || h.compareTo(MAX_SLEEP_HOURS) > 0) {
                throw new IllegalArgumentException("睡眠时长需在 0 ~ 24 小时之间");
            }
            sleepHours = h;
        }
        if (sleepQuality != null
                && (sleepQuality < MIN_SLEEP_QUALITY || sleepQuality > MAX_SLEEP_QUALITY)) {
            throw new IllegalArgumentException("睡眠质量需在 1 ~ 5 之间");
        }
        if (waterCups != null) {
            this.waterCups = waterCups;
        }
        if (steps != null) {
            this.steps = steps;
        }
        if (sleepHours != null) {
            this.sleepHours = sleepHours;
        }
        if (sleepQuality != null) {
            this.sleepQuality = sleepQuality;
        }
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static DailyCheckin reconstruct(Long id, Long userId, LocalDate recordDate, Integer waterCups,
                                           Integer steps, BigDecimal sleepHours, Integer sleepQuality,
                                           LocalDateTime createTime, LocalDateTime updateTime) {
        DailyCheckin checkin = new DailyCheckin();
        checkin.id = id;
        checkin.userId = userId;
        checkin.recordDate = recordDate;
        checkin.waterCups = waterCups;
        checkin.steps = steps;
        checkin.sleepHours = sleepHours;
        checkin.sleepQuality = sleepQuality;
        checkin.createTime = createTime;
        checkin.updateTime = updateTime;
        return checkin;
    }
}
