package com.ty.hikingalone.domain.training.repository;

import com.ty.hikingalone.domain.training.entity.TrainingRecordDaily;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练项每日汇总仓储接口
 * <p>汇总表每训练项每天一行（plan+item+date 唯一），随提交双写维护；聚合查询读这里</p>
 */
public interface TrainingRecordDailyRepository {

    /**
     * 按用户和日期范围查询汇总（热力图数据源）
     */
    List<TrainingRecordDaily> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 按计划查询全部汇总（进度/完成度数据源）
     */
    List<TrainingRecordDaily> listByPlanId(Long planId);

    /**
     * 按 计划+训练项+日期 查询当日汇总（不存在返回 null）
     */
    TrainingRecordDaily findByPlanItemDate(Long planId, Long itemId, LocalDate recordDate);

    /**
     * 按训练项删除全部汇总行（编辑计划删除训练项时级联清理）
     */
    void deleteByItemId(Long itemId);

    /**
     * 按计划删除全部汇总行（物理删除计划时级联清理）
     */
    void deleteByPlanId(Long planId);

    /**
     * 保存汇总行：有 id 走更新，无 id 走插入
     */
    void save(TrainingRecordDaily daily);
}
