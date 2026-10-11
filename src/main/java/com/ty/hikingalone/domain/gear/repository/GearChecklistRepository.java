package com.ty.hikingalone.domain.gear.repository;

import com.ty.hikingalone.domain.gear.entity.GearChecklist;
import com.ty.hikingalone.domain.gear.entity.GearChecklistItem;

import java.util.List;

/**
 * 打包清单仓储接口：聚合（清单头 + 条目）读时整体装配，写通过细粒度方法由应用服务编排事务
 */
public interface GearChecklistRepository {

    /**
     * 查某用户全部清单（含条目；状态升序 + 创建时间倒序）
     */
    List<GearChecklist> listByUser(Long userId);

    /**
     * 按主键查清单（含条目；不存在返回 null）
     */
    GearChecklist findById(Long id);

    /**
     * 保存清单头：id 为空 insert（返回生成主键），否则 update
     */
    Long saveHead(GearChecklist checklist);

    /**
     * 保存条目：id 为空 insert（返回生成主键），否则 update
     */
    Long saveItem(GearChecklistItem item);

    /**
     * 删除单条条目
     */
    void deleteItem(Long itemId);

    /**
     * 删除清单（调用方负责先删条目再删头，或使用本方法原子级联）
     */
    void deleteHeadCascade(Long checklistId);
}
