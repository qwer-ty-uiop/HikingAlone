package com.ty.hikingalone.infrastructure.training.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.training.entity.TrainingRecordDaily;
import com.ty.hikingalone.domain.training.repository.TrainingRecordDailyRepository;
import com.ty.hikingalone.infrastructure.training.mapper.TrainingRecordDailyMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingRecordDailyPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练项每日汇总仓储实现
 */
@Repository
@RequiredArgsConstructor
public class TrainingRecordDailyRepositoryImpl implements TrainingRecordDailyRepository {

    private final TrainingRecordDailyMapper dailyMapper;

    @Override
    public List<TrainingRecordDaily> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return dailyMapper.selectList(
                new LambdaQueryWrapper<TrainingRecordDailyPO>()
                        .eq(TrainingRecordDailyPO::getUserId, userId)
                        .ge(TrainingRecordDailyPO::getRecordDate, startDate)
                        .le(TrainingRecordDailyPO::getRecordDate, endDate)
                        .orderByAsc(TrainingRecordDailyPO::getRecordDate)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public List<TrainingRecordDaily> listByPlanId(Long planId) {
        return dailyMapper.selectList(
                new LambdaQueryWrapper<TrainingRecordDailyPO>()
                        .eq(TrainingRecordDailyPO::getPlanId, planId)
                        .orderByAsc(TrainingRecordDailyPO::getRecordDate)
                        .orderByAsc(TrainingRecordDailyPO::getUpdateTime)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public TrainingRecordDaily findByPlanItemDate(Long planId, Long itemId, LocalDate recordDate) {
        TrainingRecordDailyPO po = dailyMapper.selectOne(
                new LambdaQueryWrapper<TrainingRecordDailyPO>()
                        .eq(TrainingRecordDailyPO::getPlanId, planId)
                        .eq(TrainingRecordDailyPO::getItemId, itemId)
                        .eq(TrainingRecordDailyPO::getRecordDate, recordDate)
        );
        return po == null ? null : toEntity(po);
    }

    @Override
    public void deleteByItemId(Long itemId) {
        dailyMapper.delete(
                new LambdaQueryWrapper<TrainingRecordDailyPO>()
                        .eq(TrainingRecordDailyPO::getItemId, itemId)
        );
    }

    @Override
    public void deleteByPlanId(Long planId) {
        dailyMapper.delete(
                new LambdaQueryWrapper<TrainingRecordDailyPO>()
                        .eq(TrainingRecordDailyPO::getPlanId, planId)
        );
    }

    @Override
    public void save(TrainingRecordDaily daily) {
        TrainingRecordDailyPO po = new TrainingRecordDailyPO();
        BeanUtils.copyProperties(daily, po);
        if (daily.getId() != null) {
            dailyMapper.updateById(po);
        } else {
            dailyMapper.insert(po);
        }
    }

    private TrainingRecordDaily toEntity(TrainingRecordDailyPO po) {
        return TrainingRecordDaily.reconstruct(
                po.getId(), po.getPlanId(), po.getItemId(), po.getUserId(), po.getRecordDate(),
                po.getTotalTimes(), po.getTotalSets(), po.getCommitCount(),
                po.getCreateTime(), po.getUpdateTime());
    }
}
