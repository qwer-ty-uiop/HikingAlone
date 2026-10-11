package com.ty.hikingalone.infrastructure.goal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.goal.entity.HikeGoal;
import com.ty.hikingalone.domain.goal.repository.HikeGoalRepository;
import com.ty.hikingalone.infrastructure.goal.mapper.HikeGoalMapper;
import com.ty.hikingalone.infrastructure.goal.po.HikeGoalPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 徒步目标仓储实现
 */
@Repository
@RequiredArgsConstructor
public class HikeGoalRepositoryImpl implements HikeGoalRepository {

    private final HikeGoalMapper goalMapper;

    @Override
    public List<HikeGoal> listByUserAndYear(Long userId, int year) {
        return goalMapper.selectList(
                new LambdaQueryWrapper<HikeGoalPO>()
                        .eq(HikeGoalPO::getUserId, userId)
                        .eq(HikeGoalPO::getYear, year)
                        .orderByDesc(HikeGoalPO::getPeriodType)
                        .orderByAsc(HikeGoalPO::getMonth)
                        .orderByAsc(HikeGoalPO::getMetric)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public HikeGoal findByPeriodAndMetric(Long userId, int year, int month, int metric) {
        HikeGoalPO po = goalMapper.selectOne(
                new LambdaQueryWrapper<HikeGoalPO>()
                        .eq(HikeGoalPO::getUserId, userId)
                        .eq(HikeGoalPO::getYear, year)
                        .eq(HikeGoalPO::getMonth, month)
                        .eq(HikeGoalPO::getMetric, metric)
        );
        return po == null ? null : toEntity(po);
    }

    @Override
    public HikeGoal findById(Long id) {
        HikeGoalPO po = goalMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(HikeGoal goal) {
        HikeGoalPO po = new HikeGoalPO();
        po.setUserId(goal.getUserId());
        po.setMetric(goal.getMetric());
        po.setTargetValue(goal.getTargetValue());
        po.setPeriodType(goal.getPeriodType());
        po.setYear(goal.getYear());
        po.setMonth(goal.getMonth());
        po.setStatus(goal.getStatus());
        po.setCreateTime(goal.getCreateTime());
        po.setUpdateTime(goal.getUpdateTime());
        if (goal.getId() != null) {
            po.setId(goal.getId());
            goalMapper.updateById(po);
            return goal.getId();
        }
        goalMapper.insert(po);
        return po.getId();
    }

    @Override
    public void delete(Long id) {
        goalMapper.deleteById(id);
    }

    private HikeGoal toEntity(HikeGoalPO po) {
        return HikeGoal.reconstruct(
                po.getId(), po.getUserId(), po.getMetric(), po.getTargetValue(),
                po.getPeriodType(), po.getYear(), po.getMonth(), po.getStatus(),
                po.getCreateTime(), po.getUpdateTime());
    }
}
