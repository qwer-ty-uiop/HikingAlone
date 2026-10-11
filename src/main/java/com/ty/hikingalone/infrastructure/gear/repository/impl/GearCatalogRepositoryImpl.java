package com.ty.hikingalone.infrastructure.gear.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.gear.entity.GearCatalogItem;
import com.ty.hikingalone.domain.gear.repository.GearCatalogRepository;
import com.ty.hikingalone.infrastructure.gear.mapper.GearCatalogMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearCatalogPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;

/**
 * 常用装备目录仓储实现：全量载入 + 内存过滤（目录量级恒定小，且支持前缀优先排序）
 */
@Repository
@RequiredArgsConstructor
public class GearCatalogRepositoryImpl implements GearCatalogRepository {

    private final GearCatalogMapper catalogMapper;

    @Override
    public List<GearCatalogItem> search(String keyword, int limit) {
        List<GearCatalogItem> all = catalogMapper.selectList(
                        new LambdaQueryWrapper<GearCatalogPO>()
                                .orderByAsc(GearCatalogPO::getSort)
                                .orderByAsc(GearCatalogPO::getId)
                ).stream()
                .map(this::toEntity)
                .toList();
        if (keyword == null || keyword.isBlank()) {
            return all.stream().limit(limit).toList();
        }
        String k = keyword.trim();
        String kLower = k.toLowerCase();
        // 匹配名称或品牌（大小写不敏感，「gregory」命中品牌列）；前缀命中优先，同档按目录顺序
        Comparator<GearCatalogItem> byPrefixFirst = Comparator.comparingInt(
                item -> item.getName().toLowerCase().startsWith(kLower) ? 0 : 1);
        return all.stream()
                .filter(item -> item.getName().toLowerCase().contains(kLower)
                        || (item.getBrand() != null && item.getBrand().toLowerCase().contains(kLower)))
                .sorted(byPrefixFirst.thenComparing(GearCatalogItem::getSort)
                        .thenComparing(GearCatalogItem::getId))
                .limit(limit)
                .toList();
    }

    private GearCatalogItem toEntity(GearCatalogPO po) {
        return GearCatalogItem.reconstruct(
                po.getId(), po.getName(), po.getCategory(), po.getBrand(), po.getWeightGrams(), po.getSort());
    }
}
