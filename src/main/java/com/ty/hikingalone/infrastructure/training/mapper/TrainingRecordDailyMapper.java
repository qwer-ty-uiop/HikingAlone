package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingRecordDailyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 训练项每日汇总 Mapper
 */
@Mapper
public interface TrainingRecordDailyMapper extends BaseMapper<TrainingRecordDailyPO> {
}
