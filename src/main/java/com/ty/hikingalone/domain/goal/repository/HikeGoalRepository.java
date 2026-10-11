package com.ty.hikingalone.domain.goal.repository;

import com.ty.hikingalone.domain.goal.entity.HikeGoal;

import java.util.List;

/**
 * 徒步目标仓储接口
 */
public interface HikeGoalRepository {

    /**
     * 查某用户某年的全部目标（年度在前、月度按月升序、指标升序）
     */
    List<HikeGoal> listByUserAndYear(Long userId, int year);

    /**
     * 查某用户某周期某指标的目标（查重；月度 month 1~12，年度传 0）
     */
    HikeGoal findByPeriodAndMetric(Long userId, int year, int month, int metric);

    /**
     * 按主键查目标
     */
    HikeGoal findById(Long id);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(HikeGoal goal);

    /**
     * 按主键物理删除
     */
    void delete(Long id);
}
