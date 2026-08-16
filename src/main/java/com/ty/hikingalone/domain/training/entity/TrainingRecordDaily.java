package com.ty.hikingalone.domain.training.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练项每日汇总记录（汇总表，每训练项每天一行）
 * <p>由事件表 training_record 在每次提交时双写聚合：当日提交次数、按模式累计完成量。
 * 进度/剩余/热力图等聚合查询读本表，事件表只保留提交明细</p>
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrainingRecordDaily {

    private Long id;

    private Long planId;

    private Long itemId;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 当日累计次数（times模式=Σcompleted_times；sets模式=Σ每组次数）
     */
    private Integer totalTimes;

    /**
     * 当日累计组数（sets模式=Σcompleted_sets；times模式=0）
     */
    private Integer totalSets;

    /**
     * 当日提交次数（热力图 count 数据源）
     */
    private Integer commitCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：当日首条提交 → 新建汇总行（commitCount=1）
     */
    public static TrainingRecordDaily from(TrainingRecord record) {
        TrainingRecordDaily daily = new TrainingRecordDaily();
        daily.setPlanId(record.getPlanId());
        daily.setItemId(record.getItemId());
        daily.setUserId(record.getUserId());
        daily.setRecordDate(record.getRecordDate());
        daily.setTotalTimes(record.getCompletedTimes());
        daily.setTotalSets(record.getCompletedSets());
        daily.setCommitCount(1);
        LocalDateTime now = LocalDateTime.now();
        daily.setCreateTime(now);
        daily.setUpdateTime(now);
        return daily;
    }

    /**
     * 当日已有汇总行：把本次提交累加进汇总（提交次数 +1，完成量按模式对应字段累加）
     */
    public void merge(TrainingRecord record) {
        this.totalTimes = (this.totalTimes == null ? 0 : this.totalTimes)
                + (record.getCompletedTimes() == null ? 0 : record.getCompletedTimes());
        this.totalSets = (this.totalSets == null ? 0 : this.totalSets)
                + (record.getCompletedSets() == null ? 0 : record.getCompletedSets());
        this.commitCount = (this.commitCount == null ? 0 : this.commitCount) + 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 编辑记录后按差值调整汇总：完成量增减（提交次数不变），刷新 updateTime
     */
    public void adjust(int deltaSets, int deltaTimes) {
        this.totalTimes = (this.totalTimes == null ? 0 : this.totalTimes) + deltaTimes;
        this.totalSets = (this.totalSets == null ? 0 : this.totalSets) + deltaSets;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 重建工厂：从持久化数据恢复汇总行，不执行业务校验（数据已合法落库）
     */
    public static TrainingRecordDaily reconstruct(Long id, Long planId, Long itemId, Long userId,
                                                  LocalDate recordDate, Integer totalTimes, Integer totalSets,
                                                  Integer commitCount, LocalDateTime createTime, LocalDateTime updateTime) {
        TrainingRecordDaily daily = new TrainingRecordDaily();
        daily.setId(id);
        daily.setPlanId(planId);
        daily.setItemId(itemId);
        daily.setUserId(userId);
        daily.setRecordDate(recordDate);
        daily.setTotalTimes(totalTimes);
        daily.setTotalSets(totalSets);
        daily.setCommitCount(commitCount);
        daily.setCreateTime(createTime);
        daily.setUpdateTime(updateTime);
        return daily;
    }
}
