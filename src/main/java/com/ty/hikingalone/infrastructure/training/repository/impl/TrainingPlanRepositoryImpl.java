package com.ty.hikingalone.infrastructure.training.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingPlanItem;
import com.ty.hikingalone.domain.training.repository.TrainingPlanRepository;
import com.ty.hikingalone.infrastructure.training.mapper.TrainingPlanItemMapper;
import com.ty.hikingalone.infrastructure.training.mapper.TrainingPlanMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanItemPO;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练计划仓储实现：领域接口的数据库实现
 */
@Repository
@RequiredArgsConstructor
public class TrainingPlanRepositoryImpl implements TrainingPlanRepository {

    private final TrainingPlanMapper planMapper;
    private final TrainingPlanItemMapper itemMapper;

    @Override
    @Transactional
    public Long save(TrainingPlan plan) {
        TrainingPlanPO planPO = new TrainingPlanPO();
        BeanUtils.copyProperties(plan, planPO);
        planPO.setCreateTime(LocalDateTime.now());
        planPO.setUpdateTime(LocalDateTime.now());
        planMapper.insert(planPO);
        plan.setId(planPO.getId());

        for (TrainingPlanItem item : plan.getItems()) {
            TrainingPlanItemPO itemPO = new TrainingPlanItemPO();
            BeanUtils.copyProperties(item, itemPO);
            itemPO.setId(null);
            itemPO.setPlanId(planPO.getId());
            itemMapper.insert(itemPO);
        }
        return planPO.getId();
    }

    @Override
    public TrainingPlan findById(Long id) {
        TrainingPlanPO po = planMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<TrainingPlan> listByUserId(Long userId) {
        return planMapper.selectList(
                new LambdaQueryWrapper<TrainingPlanPO>()
                        .eq(TrainingPlanPO::getUserId, userId)
                        .orderByDesc(TrainingPlanPO::getCreateTime)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public List<TrainingPlanItem> listItemsByPlanId(Long planId) {
        return itemMapper.selectList(
                new LambdaQueryWrapper<TrainingPlanItemPO>()
                        .eq(TrainingPlanItemPO::getPlanId, planId)
                        .orderByAsc(TrainingPlanItemPO::getSort)
        ).stream().map(this::toItemEntity).toList();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        TrainingPlanPO po = new TrainingPlanPO();
        po.setId(id);
        po.setStatus(status);
        po.setUpdateTime(LocalDateTime.now());
        planMapper.updateById(po);
    }

    private TrainingPlan toEntity(TrainingPlanPO po) {
        return TrainingPlan.reconstruct(
                po.getId(), po.getUserId(), po.getTitle(), po.getDescription(),
                po.getStartDate(), po.getEndDate(), po.getStatus(),
                po.getCreateTime(), po.getUpdateTime());
    }

    private TrainingPlanItem toItemEntity(TrainingPlanItemPO po) {
        return TrainingPlanItem.reconstruct(
                po.getId(), po.getPlanId(), po.getName(), po.getMode(),
                po.getTotalTimes(), po.getTotalSets(), po.getUnit(), po.getSort());
    }
}
