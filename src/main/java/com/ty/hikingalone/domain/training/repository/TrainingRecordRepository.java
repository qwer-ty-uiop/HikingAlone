package com.ty.hikingalone.domain.training.repository;

import com.ty.hikingalone.domain.training.entity.TrainingRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日训练记录仓储接口
 */
public interface TrainingRecordRepository {

    /**
     * 按用户和日期范围查询记录（热力图数据源）
     */
    List<TrainingRecord> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 按计划查询全部记录（计划详情回填用）
     */
    List<TrainingRecord> listByPlanId(Long planId);

    /**
     * 按 计划+训练项+日期 保存或更新记录（重复提交走更新）
     */
    void saveOrUpdate(TrainingRecord record);
}
