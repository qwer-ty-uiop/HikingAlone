package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练计划项 Mapper
 */
@Mapper
public interface TrainingPlanItemMapper extends BaseMapper<TrainingPlanItemPO> {
}
