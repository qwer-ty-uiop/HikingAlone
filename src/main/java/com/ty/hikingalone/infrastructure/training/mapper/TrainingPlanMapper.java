package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingPlanPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练计划 Mapper
 */
@Mapper
public interface TrainingPlanMapper extends BaseMapper<TrainingPlanPO> {
}
