package com.ty.hikingalone.infrastructure.daily.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.daily.entity.DailyCheckin;
import com.ty.hikingalone.domain.daily.repository.DailyCheckinRepository;
import com.ty.hikingalone.infrastructure.daily.mapper.DailyCheckinMapper;
import com.ty.hikingalone.infrastructure.daily.po.DailyCheckinPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日打卡仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DailyCheckinRepositoryImpl implements DailyCheckinRepository {

    private final DailyCheckinMapper checkinMapper;

    @Override
    public DailyCheckin findByUserAndDate(Long userId, LocalDate recordDate) {
        DailyCheckinPO po = checkinMapper.selectOne(
                new LambdaQueryWrapper<DailyCheckinPO>()
                        .eq(DailyCheckinPO::getUserId, userId)
                        .eq(DailyCheckinPO::getRecordDate, recordDate)
        );
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<DailyCheckin> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return checkinMapper.selectList(
                new LambdaQueryWrapper<DailyCheckinPO>()
                        .eq(DailyCheckinPO::getUserId, userId)
                        .ge(DailyCheckinPO::getRecordDate, startDate)
                        .le(DailyCheckinPO::getRecordDate, endDate)
                        .orderByAsc(DailyCheckinPO::getRecordDate)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public Long save(DailyCheckin checkin) {
        DailyCheckinPO po = new DailyCheckinPO();
        po.setUserId(checkin.getUserId());
        po.setRecordDate(checkin.getRecordDate());
        po.setWaterCups(checkin.getWaterCups());
        po.setSteps(checkin.getSteps());
        po.setSleepHours(checkin.getSleepHours());
        po.setSleepQuality(checkin.getSleepQuality());
        po.setCreateTime(checkin.getCreateTime());
        po.setUpdateTime(checkin.getUpdateTime());
        if (checkin.getId() != null) {
            po.setId(checkin.getId());
            checkinMapper.updateById(po);
            return checkin.getId();
        }
        checkinMapper.insert(po);
        return po.getId();
    }

    private DailyCheckin toEntity(DailyCheckinPO po) {
        return DailyCheckin.reconstruct(
                po.getId(), po.getUserId(), po.getRecordDate(), po.getWaterCups(),
                po.getSteps(), po.getSleepHours(), po.getSleepQuality(),
                po.getCreateTime(), po.getUpdateTime());
    }
}
