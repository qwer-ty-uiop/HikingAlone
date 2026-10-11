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
        // 归一化匹配：去所有空白 + 小写（「格里高利B65」=「格里高利 B65」，名称/品牌/中文别名都命中）
        String k = normalize(keyword.trim());
        Comparator<GearCatalogItem> byPrefixFirst = Comparator.comparingInt(
                item -> normalize(item.getName()).startsWith(k) ? 0 : 1);
        return all.stream()
                .filter(item -> normalize(item.getName()).contains(k)
                        || (item.getBrand() != null && normalize(item.getBrand()).contains(k))
                        || (item.getSearchTerms() != null && normalize(item.getSearchTerms()).contains(k)))
                .sorted(byPrefixFirst.thenComparing(GearCatalogItem::getSort)
                        .thenComparing(GearCatalogItem::getId))
                .limit(limit)
                .toList();
    }

    /** 归一化：去所有空白 + 小写（中文输入习惯性不加空格，数据库里带空格） */
    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "").toLowerCase();
    }

    private GearCatalogItem toEntity(GearCatalogPO po) {
        return GearCatalogItem.reconstruct(
                po.getId(), po.getName(), po.getCategory(), po.getBrand(), po.getSearchTerms(),
                po.getWeightGrams(), po.getSort());
    }
}
