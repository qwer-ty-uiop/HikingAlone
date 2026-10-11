package com.ty.hikingalone.infrastructure.journal.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.infrastructure.journal.mapper.HikeJournalMapper;
import com.ty.hikingalone.infrastructure.journal.po.HikeJournalPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 徒步日记仓储实现
 */
@Repository
@RequiredArgsConstructor
public class HikeJournalRepositoryImpl implements HikeJournalRepository {

    private final HikeJournalMapper journalMapper;

    @Override
    public List<HikeJournal> listByUser(Long userId) {
        return journalMapper.selectList(
                new LambdaQueryWrapper<HikeJournalPO>()
                        .eq(HikeJournalPO::getUserId, userId)
                        .orderByDesc(HikeJournalPO::getHikeDate)
                        .orderByDesc(HikeJournalPO::getId)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public List<HikeJournal> listByUserAndYear(Long userId, int year) {
        return journalMapper.selectList(
                new LambdaQueryWrapper<HikeJournalPO>()
                        .eq(HikeJournalPO::getUserId, userId)
                        .ge(HikeJournalPO::getHikeDate, LocalDate.of(year, 1, 1))
                        .le(HikeJournalPO::getHikeDate, LocalDate.of(year, 12, 31))
                        .orderByDesc(HikeJournalPO::getHikeDate)
                        .orderByDesc(HikeJournalPO::getId)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public List<HikeJournal> listRecent(Long userId, int limit) {
        return journalMapper.selectList(
                new LambdaQueryWrapper<HikeJournalPO>()
                        .eq(HikeJournalPO::getUserId, userId)
                        .orderByDesc(HikeJournalPO::getHikeDate)
                        .orderByDesc(HikeJournalPO::getId)
                        .last("LIMIT " + limit)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public HikeJournal findById(Long id) {
        HikeJournalPO po = journalMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(HikeJournal journal) {
        HikeJournalPO po = toPO(journal);
        if (journal.getId() != null) {
            po.setId(journal.getId());
            journalMapper.updateById(po);
            return journal.getId();
        }
        journalMapper.insert(po);
        return po.getId();
    }

    @Override
    public void delete(Long id) {
        journalMapper.deleteById(id);
    }

    private HikeJournalPO toPO(HikeJournal journal) {
        HikeJournalPO po = new HikeJournalPO();
        po.setUserId(journal.getUserId());
        po.setTitle(journal.getTitle());
        po.setHikeDate(journal.getHikeDate());
        po.setRouteId(journal.getRouteId());
        po.setDistanceKm(journal.getDistanceKm());
        po.setDurationMin(journal.getDurationMin());
        po.setElevationGain(journal.getElevationGain());
        po.setDifficulty(journal.getDifficulty());
        po.setMood(journal.getMood());
        po.setWeather(journal.getWeather());
        po.setCompanions(journal.getCompanions());
        po.setCost(journal.getCost());
        po.setContent(journal.getContent());
        po.setCreateTime(journal.getCreateTime());
        po.setUpdateTime(journal.getUpdateTime());
        return po;
    }

    private HikeJournal toEntity(HikeJournalPO po) {
        return HikeJournal.reconstruct(
                po.getId(), po.getUserId(), po.getTitle(), po.getHikeDate(), po.getRouteId(),
                po.getDistanceKm(), po.getDurationMin(), po.getElevationGain(),
                po.getDifficulty(), po.getMood(), po.getWeather(), po.getCompanions(),
                po.getCost(), po.getContent(), po.getCreateTime(), po.getUpdateTime());
    }
}
