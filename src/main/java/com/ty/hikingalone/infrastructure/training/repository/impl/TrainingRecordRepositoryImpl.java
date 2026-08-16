package com.ty.hikingalone.infrastructure.training.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.training.entity.TrainingRecord;
import com.ty.hikingalone.domain.training.repository.TrainingRecordRepository;
import com.ty.hikingalone.infrastructure.training.mapper.TrainingRecordMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingRecordPO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日训练记录仓储实现
 */
@Repository
@RequiredArgsConstructor
public class TrainingRecordRepositoryImpl implements TrainingRecordRepository {

    private final TrainingRecordMapper recordMapper;

    @Override
    public List<TrainingRecord> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<TrainingRecordPO>()
                        .eq(TrainingRecordPO::getUserId, userId)
                        .ge(TrainingRecordPO::getRecordDate, startDate)
                        .le(TrainingRecordPO::getRecordDate, endDate)
                        .orderByAsc(TrainingRecordPO::getRecordDate)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public List<TrainingRecord> listByPlanId(Long planId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<TrainingRecordPO>()
                        .eq(TrainingRecordPO::getPlanId, planId)
                        .orderByAsc(TrainingRecordPO::getRecordDate)
                        .orderByAsc(TrainingRecordPO::getCreateTime)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public void insert(TrainingRecord record) {
        TrainingRecordPO po = new TrainingRecordPO();
        BeanUtils.copyProperties(record, po);
        recordMapper.insert(po);
    }

    private TrainingRecord toEntity(TrainingRecordPO po) {
        return TrainingRecord.reconstruct(
                po.getId(), po.getPlanId(), po.getItemId(), po.getUserId(),
                po.getRecordDate(), po.getCompletedSets(), po.getCompletedTimes(),
                po.getCreateTime(), po.getUpdateTime());
    }
}
