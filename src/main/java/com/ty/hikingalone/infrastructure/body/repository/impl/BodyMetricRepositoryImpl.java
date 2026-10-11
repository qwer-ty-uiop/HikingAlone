package com.ty.hikingalone.infrastructure.body.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.body.entity.BodyMetric;
import com.ty.hikingalone.domain.body.repository.BodyMetricRepository;
import com.ty.hikingalone.infrastructure.body.mapper.BodyMetricMapper;
import com.ty.hikingalone.infrastructure.body.po.BodyMetricPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日身体数据仓储实现
 */
@Repository
@RequiredArgsConstructor
public class BodyMetricRepositoryImpl implements BodyMetricRepository {

    private final BodyMetricMapper bodyMetricMapper;

    @Override
    public BodyMetric findByUserAndDate(Long userId, LocalDate recordDate) {
        BodyMetricPO po = bodyMetricMapper.selectOne(
                new LambdaQueryWrapper<BodyMetricPO>()
                        .eq(BodyMetricPO::getUserId, userId)
                        .eq(BodyMetricPO::getRecordDate, recordDate)
        );
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<BodyMetric> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return bodyMetricMapper.selectList(
                new LambdaQueryWrapper<BodyMetricPO>()
                        .eq(BodyMetricPO::getUserId, userId)
                        .ge(BodyMetricPO::getRecordDate, startDate)
                        .le(BodyMetricPO::getRecordDate, endDate)
                        .orderByAsc(BodyMetricPO::getRecordDate)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public Long save(BodyMetric metric) {
        BodyMetricPO po = new BodyMetricPO();
        po.setUserId(metric.getUserId());
        po.setRecordDate(metric.getRecordDate());
        po.setWeightKg(metric.getWeightKg());
        po.setRestingHeartRate(metric.getRestingHeartRate());
        po.setNote(metric.getNote());
        po.setCreateTime(metric.getCreateTime());
        po.setUpdateTime(metric.getUpdateTime());
        if (metric.getId() != null) {
            po.setId(metric.getId());
            bodyMetricMapper.updateById(po);
            return metric.getId();
        }
        bodyMetricMapper.insert(po);
        return po.getId();
    }

    private BodyMetric toEntity(BodyMetricPO po) {
        return BodyMetric.reconstruct(
                po.getId(), po.getUserId(), po.getRecordDate(), po.getWeightKg(),
                po.getRestingHeartRate(), po.getNote(), po.getCreateTime(), po.getUpdateTime());
    }
}
