package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 训练计划项 Mapper
 */
@Mapper
public interface TrainingPlanItemMapper extends BaseMapper<TrainingPlanItemPO> {

    /**
     * 批量插入训练项（创建计划时一次写多条，避免 N+1 单条插入），SQL 见 mapper/TrainingPlanItemMapper.xml
     */
    int insertBatch(@Param("items") List<TrainingPlanItemPO> items);
}