package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanItemPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 训练计划项 Mapper
 */
@Mapper
public interface TrainingPlanItemMapper extends BaseMapper<TrainingPlanItemPO> {

    /**
     * 批量插入训练项（创建计划时一次写多条，避免 N+1 单条插入）
     */
    @Insert("<script>" +
            "INSERT INTO training_plan_item (plan_id, name, mode, total_times, total_sets, unit, sort) VALUES " +
            "<foreach collection='items' item='it' separator=','>" +
            "(#{it.planId}, #{it.name}, #{it.mode}, #{it.totalTimes}, #{it.totalSets}, #{it.unit}, #{it.sort})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("items") List<TrainingPlanItemPO> items);
}