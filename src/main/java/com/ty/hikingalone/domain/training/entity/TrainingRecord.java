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
 * 每日训练记录领域实体
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrainingRecord {

    private Long id;

    private Long planId;

    private Long itemId;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 当天完成组数（sets模式；times模式为0）
     */
    private Integer completedSets;

    /**
     * 当天完成次数（times模式=当天总次数；sets模式=每组次数，默认取计划值）
     */
    private Integer completedTimes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：按训练项制定模式组装一条当天完成记录
     * <p>times模式：completedTimes=当天总次数（缺省记0），completedSets记0；
     * sets模式：completedSets=当天组数（缺省记0），completedTimes=每组次数，不传则取计划默认值</p>
     */
    public static TrainingRecord submit(Long userId, Long planId, TrainingPlanItem item,
                                        LocalDate recordDate,
                                        Integer completedSets, Integer completedTimes) {
        if (item == null) {
            throw new IllegalArgumentException("训练项不存在");
        }
        if (recordDate == null) {
            throw new IllegalArgumentException("记录日期不能为空");
        }
        TrainingRecord record = new TrainingRecord();
        record.setPlanId(planId);
        record.setItemId(item.getId());
        record.setUserId(userId);
        record.setRecordDate(recordDate);
        if (item.isSetsMode()) {
            record.setCompletedSets(completedSets == null ? 0 : completedSets);
            record.setCompletedTimes(completedTimes != null ? completedTimes : item.getTotalTimes());
        } else {
            record.setCompletedSets(0);
            record.setCompletedTimes(completedTimes == null ? 0 : completedTimes);
        }
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    /**
     * 重建工厂：从持久化数据恢复记录，不执行业务校验（数据已合法落库）
     */
    public static TrainingRecord reconstruct(Long id, Long planId, Long itemId, Long userId,
                                             LocalDate recordDate, Integer completedSets, Integer completedTimes,
                                             LocalDateTime createTime, LocalDateTime updateTime) {
        TrainingRecord record = new TrainingRecord();
        record.setId(id);
        record.setPlanId(planId);
        record.setItemId(itemId);
        record.setUserId(userId);
        record.setRecordDate(recordDate);
        record.setCompletedSets(completedSets);
        record.setCompletedTimes(completedTimes);
        record.setCreateTime(createTime);
        record.setUpdateTime(updateTime);
        return record;
    }
}
