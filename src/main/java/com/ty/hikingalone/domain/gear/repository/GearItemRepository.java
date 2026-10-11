package com.ty.hikingalone.domain.gear.repository;

import com.ty.hikingalone.domain.gear.entity.GearItem;

import java.util.List;

/**
 * 装备台账仓储接口
 */
public interface GearItemRepository {

    /**
     * 查某用户全部装备（在役在前、品类升序、id 升序）
     */
    List<GearItem> listByUser(Long userId);

    /**
     * 按主键查装备
     */
    GearItem findById(Long id);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(GearItem item);

    /**
     * 按主键物理删除
     */
    void delete(Long id);
}
