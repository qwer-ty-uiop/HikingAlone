package com.ty.hikingalone.domain.goal.entity;

import com.ty.hikingalone.common.enums.goal.GoalMetricEnum;
import com.ty.hikingalone.common.enums.goal.GoalPeriodTypeEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 徒步目标领域实体（聚合根）
 * <p>metric/period 创建后不可变（update 只许改 target/status），避免「改口径」导致的进度语义漂移；
 * 达成与否是读时计算，不落库。month=0 为年度哨兵值，保证 uk_user_period_metric 可用</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HikeGoal {

    /** 年份下限 */
    public static final int MIN_YEAR = 2020;

    /** 年份上限 */
    public static final int MAX_YEAR = 2100;

    /** 目标状态：已放弃 */
    public static final int STATUS_ABANDONED = 0;

    /** 目标状态：进行中 */
    public static final int STATUS_ACTIVE = 1;

    private Long id;

    private Long userId;

    /**
     * 指标（GoalMetricEnum.code）
     */
    private Integer metric;

    /**
     * 目标值（按 metric 钳制，1 位小数）
     */
    private BigDecimal targetValue;

    /**
     * 周期类型（GoalPeriodTypeEnum.code）
     */
    private Integer periodType;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月 [1,12]；年度固定 0（哨兵值）
     */
    private Integer month;

    /**
     * 状态 0 已放弃 / 1 进行中
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：新建目标（metric 决定 periodType 与目标值区间；月度 month 必填，年度 month 置 0）
     */
    public static HikeGoal create(Long userId, Integer metric, BigDecimal targetValue, Integer year, Integer month) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        GoalMetricEnum metricEnum = GoalMetricEnum.fromCode(metric);
        if (year == null || year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("年份需在 2020 ~ 2100 之间");
        }
        BigDecimal target = targetValue == null ? null : targetValue.setScale(1, RoundingMode.HALF_UP);
        BigDecimal min = new BigDecimal(metricEnum.getTargetRange()[0]);
        BigDecimal max = new BigDecimal(metricEnum.getTargetRange()[1]);
        if (target == null || target.compareTo(min) < 0 || target.compareTo(max) > 0) {
            throw new IllegalArgumentException("目标值需在 " + min.stripTrailingZeros().toPlainString()
                    + " ~ " + max.stripTrailingZeros().toPlainString() + " " + metricEnum.getUnit() + " 之间");
        }
        HikeGoal goal = new HikeGoal();
        goal.userId = userId;
        goal.metric = metric;
        goal.targetValue = target;
        goal.periodType = metricEnum.getPeriodType().getCode();
        goal.year = year;
        if (metricEnum.getPeriodType() == GoalPeriodTypeEnum.MONTHLY) {
            if (month == null || month < 1 || month > 12) {
                throw new IllegalArgumentException("月度目标需指定 1 ~ 12 月");
            }
            goal.month = month;
        } else {
            goal.month = 0;
        }
        goal.status = STATUS_ACTIVE;
        goal.createTime = LocalDateTime.now();
        return goal;
    }

    /**
     * 编辑目标：只许改 targetValue/status（非 null 覆盖），metric/period/year/month 不可变
     */
    public void update(BigDecimal targetValue, Integer status) {
        if (targetValue != null) {
            GoalMetricEnum metricEnum = GoalMetricEnum.fromCode(this.metric);
            BigDecimal target = targetValue.setScale(1, RoundingMode.HALF_UP);
            BigDecimal min = new BigDecimal(metricEnum.getTargetRange()[0]);
            BigDecimal max = new BigDecimal(metricEnum.getTargetRange()[1]);
            if (target.compareTo(min) < 0 || target.compareTo(max) > 0) {
                throw new IllegalArgumentException("目标值需在 " + min.stripTrailingZeros().toPlainString()
                        + " ~ " + max.stripTrailingZeros().toPlainString() + " " + metricEnum.getUnit() + " 之间");
            }
            this.targetValue = target;
        }
        if (status != null) {
            if (status != STATUS_ABANDONED && status != STATUS_ACTIVE) {
                throw new IllegalArgumentException("目标状态只能是 0（已放弃）或 1（进行中）");
            }
            this.status = status;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static HikeGoal reconstruct(Long id, Long userId, Integer metric, BigDecimal targetValue,
                                       Integer periodType, Integer year, Integer month, Integer status,
                                       LocalDateTime createTime, LocalDateTime updateTime) {
        HikeGoal goal = new HikeGoal();
        goal.id = id;
        goal.userId = userId;
        goal.metric = metric;
        goal.targetValue = targetValue;
        goal.periodType = periodType;
        goal.year = year;
        goal.month = month;
        goal.status = status;
        goal.createTime = createTime;
        goal.updateTime = updateTime;
        return goal;
    }
}
