package com.ty.hikingalone.infrastructure.wish.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.wish.entity.HikeWish;
import com.ty.hikingalone.domain.wish.repository.HikeWishRepository;
import com.ty.hikingalone.infrastructure.wish.mapper.HikeWishMapper;
import com.ty.hikingalone.infrastructure.wish.po.HikeWishPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 徒步心愿仓储实现
 */
@Repository
@RequiredArgsConstructor
public class HikeWishRepositoryImpl implements HikeWishRepository {

    private final HikeWishMapper wishMapper;

    @Override
    public List<HikeWish> listByUser(Long userId) {
        return wishMapper.selectList(
                new LambdaQueryWrapper<HikeWishPO>()
                        .eq(HikeWishPO::getUserId, userId)
                        .orderByAsc(HikeWishPO::getStatus)
                        .orderByDesc(HikeWishPO::getPriority)
                        .orderByAsc(HikeWishPO::getExpectedDate)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public HikeWish findById(Long id) {
        HikeWishPO po = wishMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(HikeWish wish) {
        HikeWishPO po = new HikeWishPO();
        po.setUserId(wish.getUserId());
        po.setTitle(wish.getTitle());
        po.setLocation(wish.getLocation());
        po.setExpectedDate(wish.getExpectedDate());
        po.setPriority(wish.getPriority());
        po.setNote(wish.getNote());
        po.setStatus(wish.getStatus());
        po.setDoneDate(wish.getDoneDate());
        po.setJournalId(wish.getJournalId());
        po.setCreateTime(wish.getCreateTime());
        po.setUpdateTime(wish.getUpdateTime());
        if (wish.getId() != null) {
            po.setId(wish.getId());
            wishMapper.updateById(po);
            return wish.getId();
        }
        wishMapper.insert(po);
        return po.getId();
    }

    @Override
    public void delete(Long id) {
        wishMapper.deleteById(id);
    }

    private HikeWish toEntity(HikeWishPO po) {
        return HikeWish.reconstruct(
                po.getId(), po.getUserId(), po.getTitle(), po.getLocation(),
                po.getExpectedDate(), po.getPriority(), po.getNote(),
                po.getStatus(), po.getDoneDate(), po.getJournalId(),
                po.getCreateTime(), po.getUpdateTime());
    }
}
