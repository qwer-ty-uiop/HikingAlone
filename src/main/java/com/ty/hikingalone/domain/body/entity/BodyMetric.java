package com.ty.hikingalone.domain.body.entity;

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
 * 每日身体数据领域实体（聚合根）
 * <p>聚合边界 = 用户 × 日期：一天一条 Measurement，同日重复记录语义为「修正当日测量值」而非新增事件
 * （与训练打卡的 append 事件流不同——称重是测量值，重测覆盖旧值；打卡是行为事件，多次提交各自独立）。
 * 体重/心率区间、日期非未来等不变量内聚在工厂与 update 方法中</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyMetric {

    /** 体重下限 kg（成人体重合理区间，超出视为录入错误） */
    public static final BigDecimal MIN_WEIGHT = new BigDecimal("25.0");

    /** 体重上限 kg */
    public static final BigDecimal MAX_WEIGHT = new BigDecimal("250.0");

    /** 静息心率下限 次/分 */
    public static final int MIN_HEART_RATE = 30;

    /** 静息心率上限 次/分 */
    public static final int MAX_HEART_RATE = 220;

    /** 备注最大长度 */
    public static final int MAX_NOTE_LENGTH = 200;

    private Long id;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 体重 kg，1 位小数
     */
    private BigDecimal weightKg;

    /**
     * 晨起静息心率 次/分（可空）
     */
    private Integer restingHeartRate;

    /**
     * 备注（睡眠/疲劳/状态等，可空，trim 后落库）
     */
    private String note;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：首次记录某天身体数据，校验全部不变量
     */
    public static BodyMetric record(Long userId, LocalDate recordDate, BigDecimal weightKg,
                                    Integer restingHeartRate, String note) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("记录日期不能为空");
        }
        if (recordDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("记录日期不能是未来");
        }
        BodyMetric metric = new BodyMetric();
        metric.userId = userId;
        metric.recordDate = recordDate;
        metric.applyMeasurement(weightKg, restingHeartRate, note);
        metric.createTime = LocalDateTime.now();
        return metric;
    }

    /**
     * 修正当日测量值：全量替换体重/心率/备注（不变量同首次记录），并刷新 updateTime。
     * <p>不改 userId/recordDate/createTime</p>
     */
    public void update(BigDecimal weightKg, Integer restingHeartRate, String note) {
        applyMeasurement(weightKg, restingHeartRate, note);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键（insert 路径 id 由 DB 自增产生）
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 测量值组装与校验：体重必填且落在合理区间（统一保留 1 位小数）、心率可空但非空需在区间内、备注限长
     */
    private void applyMeasurement(BigDecimal weightKg, Integer restingHeartRate, String note) {
        if (weightKg == null) {
            throw new IllegalArgumentException("体重不能为空");
        }
        BigDecimal w = weightKg.setScale(1, RoundingMode.HALF_UP);
        if (w.compareTo(MIN_WEIGHT) < 0 || w.compareTo(MAX_WEIGHT) > 0) {
            throw new IllegalArgumentException("体重需在 25.0 ~ 250.0 kg 之间");
        }
        if (restingHeartRate != null
                && (restingHeartRate < MIN_HEART_RATE || restingHeartRate > MAX_HEART_RATE)) {
            throw new IllegalArgumentException("静息心率需在 30 ~ 220 次/分之间");
        }
        if (note != null && note.trim().length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("备注不能超过 200 字");
        }
        this.weightKg = w;
        this.restingHeartRate = restingHeartRate;
        this.note = note == null || note.isBlank() ? null : note.trim();
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static BodyMetric reconstruct(Long id, Long userId, LocalDate recordDate, BigDecimal weightKg,
                                         Integer restingHeartRate, String note,
                                         LocalDateTime createTime, LocalDateTime updateTime) {
        BodyMetric metric = new BodyMetric();
        metric.id = id;
        metric.userId = userId;
        metric.recordDate = recordDate;
        metric.weightKg = weightKg;
        metric.restingHeartRate = restingHeartRate;
        metric.note = note;
        metric.createTime = createTime;
        metric.updateTime = updateTime;
        return metric;
    }
}
