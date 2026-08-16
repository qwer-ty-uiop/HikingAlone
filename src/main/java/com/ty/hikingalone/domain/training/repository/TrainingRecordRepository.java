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
     * 按计划查询全部提交记录（计划详情回填用；append 模型下每次提交一条）
     */
    List<TrainingRecord> listByPlanId(Long planId);

    /**
     * 追加一条提交记录（每次提交都是一条独立记录，不做查找合并）
     */
    void insert(TrainingRecord record);
}
