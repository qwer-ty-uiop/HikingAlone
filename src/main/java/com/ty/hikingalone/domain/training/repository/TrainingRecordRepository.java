package com.ty.hikingalone.domain.training.repository;

import com.ty.hikingalone.domain.training.entity.TrainingRecord;

import java.time.LocalDate;
import java.util.Collection;
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
     * 按多个计划批量查询提交记录（避免计划列表 N+1 查询）
     */
    List<TrainingRecord> listByPlanIds(Collection<Long> planIds);

    /**
     * 按主键查询单条提交记录（不存在返回 null）
     */
    TrainingRecord findById(Long id);

    /**
     * 追加一条提交记录（每次提交都是一条独立记录，不做查找合并）
     */
    void insert(TrainingRecord record);

    /**
     * 更新单条提交记录的完成量与 updateTime（编辑打卡用）
     */
    void update(TrainingRecord record);

    /**
     * 按训练项删除全部提交记录（编辑计划删除训练项时级联清理）
     */
    void deleteByItemId(Long itemId);

    /**
     * 按计划删除全部提交记录（物理删除计划时级联清理）
     */
    void deleteByPlanId(Long planId);
}
