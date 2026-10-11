package com.ty.hikingalone.domain.gear.repository;

import com.ty.hikingalone.domain.gear.entity.GearCatalogItem;

import java.util.List;

/**
 * 常用装备目录仓储接口（平台级共享只读数据）
 */
public interface GearCatalogRepository {

    /**
     * 关键词联想查询：name 包含匹配，前缀命中优先，再按 sort/id 排序（平台量级恒定小，内存过滤）
     *
     * @param keyword 可空（空则返回前 limit 条供浏览）
     * @param limit   返回上限（钳制由应用服务负责）
     */
    List<GearCatalogItem> search(String keyword, int limit);
}
