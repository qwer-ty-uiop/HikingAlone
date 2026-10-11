package com.ty.hikingalone.infrastructure.gear.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.gear.entity.GearChecklist;
import com.ty.hikingalone.domain.gear.entity.GearChecklistItem;
import com.ty.hikingalone.domain.gear.repository.GearChecklistRepository;
import com.ty.hikingalone.infrastructure.gear.mapper.GearChecklistItemMapper;
import com.ty.hikingalone.infrastructure.gear.mapper.GearChecklistMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearChecklistItemPO;
import com.ty.hikingalone.infrastructure.gear.po.GearChecklistPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 打包清单仓储实现：聚合读时整体装配（头 + 条目），写走细粒度方法
 */
@Repository
@RequiredArgsConstructor
public class GearChecklistRepositoryImpl implements GearChecklistRepository {

    private final GearChecklistMapper checklistMapper;
    private final GearChecklistItemMapper itemMapper;

    @Override
    public List<GearChecklist> listByUser(Long userId) {
        List<GearChecklistPO> heads = checklistMapper.selectList(
                new LambdaQueryWrapper<GearChecklistPO>()
                        .eq(GearChecklistPO::getUserId, userId)
                        .orderByAsc(GearChecklistPO::getStatus)
                        .orderByDesc(GearChecklistPO::getCreateTime)
        );
        if (heads.isEmpty()) {
            return List.of();
        }
        Map<Long, List<GearChecklistItem>> itemsByChecklist = loadItems(
                heads.stream().map(GearChecklistPO::getId).toList());
        return heads.stream()
                .map(head -> toEntity(head, itemsByChecklist.getOrDefault(head.getId(), List.of())))
                .toList();
    }

    @Override
    public GearChecklist findById(Long id) {
        GearChecklistPO head = checklistMapper.selectById(id);
        if (head == null) {
            return null;
        }
        Map<Long, List<GearChecklistItem>> itemsByChecklist =
                loadItems(List.of(head.getId()));
        return toEntity(head, itemsByChecklist.getOrDefault(head.getId(), List.of()));
    }

    @Override
    public Long saveHead(GearChecklist checklist) {
        GearChecklistPO po = new GearChecklistPO();
        po.setUserId(checklist.getUserId());
        po.setTitle(checklist.getTitle());
        po.setHikeDate(checklist.getHikeDate());
        po.setNote(checklist.getNote());
        po.setStatus(checklist.getStatus());
        po.setCreateTime(checklist.getCreateTime());
        po.setUpdateTime(checklist.getUpdateTime());
        if (checklist.getId() != null) {
            po.setId(checklist.getId());
            checklistMapper.updateById(po);
            return checklist.getId();
        }
        checklistMapper.insert(po);
        return po.getId();
    }

    @Override
    public Long saveItem(GearChecklistItem item) {
        GearChecklistItemPO po = new GearChecklistItemPO();
        po.setChecklistId(item.getChecklistId());
        po.setGearItemId(item.getGearItemId());
        po.setName(item.getName());
        po.setWeightGrams(item.getWeightGrams());
        po.setChecked(item.getChecked());
        po.setSort(item.getSort());
        po.setCreateTime(item.getCreateTime());
        if (item.getId() != null) {
            po.setId(item.getId());
            itemMapper.updateById(po);
            return item.getId();
        }
        itemMapper.insert(po);
        return po.getId();
    }

    @Override
    public void deleteItem(Long itemId) {
        itemMapper.deleteById(itemId);
    }

    @Override
    @Transactional
    public void deleteHeadCascade(Long checklistId) {
        itemMapper.delete(new LambdaQueryWrapper<GearChecklistItemPO>()
                .eq(GearChecklistItemPO::getChecklistId, checklistId));
        checklistMapper.deleteById(checklistId);
    }

    /**
     * 批量装配条目并按 checklistId 分组（条目按 sort 升序）
     */
    private Map<Long, List<GearChecklistItem>> loadItems(List<Long> checklistIds) {
        return itemMapper.selectList(
                        new LambdaQueryWrapper<GearChecklistItemPO>()
                                .in(GearChecklistItemPO::getChecklistId, checklistIds)
                                .orderByAsc(GearChecklistItemPO::getSort)
                ).stream()
                .map(this::toItemEntity)
                .collect(Collectors.groupingBy(GearChecklistItem::getChecklistId));
    }

    private GearChecklist toEntity(GearChecklistPO po, List<GearChecklistItem> items) {
        return GearChecklist.reconstruct(
                po.getId(), po.getUserId(), po.getTitle(), po.getHikeDate(), po.getNote(),
                po.getStatus(), po.getCreateTime(), po.getUpdateTime(), items);
    }

    private GearChecklistItem toItemEntity(GearChecklistItemPO po) {
        return GearChecklistItem.reconstruct(
                po.getId(), po.getChecklistId(), po.getGearItemId(), po.getName(),
                po.getWeightGrams(), po.getChecked(), po.getSort(), po.getCreateTime());
    }
}
