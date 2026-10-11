package com.ty.hikingalone.infrastructure.gear.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.gear.entity.GearItem;
import com.ty.hikingalone.domain.gear.repository.GearItemRepository;
import com.ty.hikingalone.infrastructure.gear.mapper.GearItemMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearItemPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 装备台账仓储实现
 */
@Repository
@RequiredArgsConstructor
public class GearItemRepositoryImpl implements GearItemRepository {

    private final GearItemMapper gearItemMapper;

    @Override
    public List<GearItem> listByUser(Long userId) {
        return gearItemMapper.selectList(
                new LambdaQueryWrapper<GearItemPO>()
                        .eq(GearItemPO::getUserId, userId)
                        .orderByAsc(GearItemPO::getStatus)
                        .orderByAsc(GearItemPO::getCategory)
                        .orderByAsc(GearItemPO::getId)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public GearItem findById(Long id) {
        GearItemPO po = gearItemMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(GearItem item) {
        GearItemPO po = new GearItemPO();
        po.setUserId(item.getUserId());
        po.setName(item.getName());
        po.setCategory(item.getCategory());
        po.setWeightGrams(item.getWeightGrams());
        po.setBrand(item.getBrand());
        po.setNote(item.getNote());
        po.setStatus(item.getStatus());
        po.setCreateTime(item.getCreateTime());
        po.setUpdateTime(item.getUpdateTime());
        if (item.getId() != null) {
            po.setId(item.getId());
            gearItemMapper.updateById(po);
            return item.getId();
        }
        gearItemMapper.insert(po);
        return po.getId();
    }

    @Override
    public void delete(Long id) {
        gearItemMapper.deleteById(id);
    }

    private GearItem toEntity(GearItemPO po) {
        return GearItem.reconstruct(
                po.getId(), po.getUserId(), po.getName(), po.getCategory(), po.getWeightGrams(),
                po.getBrand(), po.getNote(), po.getStatus(), po.getCreateTime(), po.getUpdateTime());
    }
}
