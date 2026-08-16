package com.ty.hikingalone.domain.training.repository;

import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingPlanItem;

import java.util.List;

/**
 * 训练计划仓储接口
 * <p>以训练计划为聚合根，管理计划及其训练项</p>
 */
public interface TrainingPlanRepository {

    /**
     * 保存计划（含其内部训练项），返回计划id
     */
    Long save(TrainingPlan plan);

    /**
     * 更新计划级字段（title/description/startDate/endDate），并刷新 updateTime
     */
    void update(TrainingPlan plan);

    /**
     * 新增一条训练项（planId 由仓储统一赋值），返回后回填 id
     */
    void saveItem(TrainingPlanItem item, Long planId);

    /**
     * 更新一条训练项
     */
    void updateItem(TrainingPlanItem item);

    /**
     * 删除一条训练项
     */
    void deleteItem(Long itemId);

    /**
     * 按id查询计划
     */
    TrainingPlan findById(Long id);

    /**
     * 按用户查询计划列表，按创建时间倒序
     */
    List<TrainingPlan> listByUserId(Long userId);

    /**
     * 按计划查询训练项列表，按排序号升序
     */
    List<TrainingPlanItem> listItemsByPlanId(Long planId);

    /**
     * 更新计划状态
     */
    void updateStatus(Long id, Integer status);
}
