package com.ty.hikingalone.infrastructure.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.training.po.TrainingRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日训练记录 Mapper
 */
@Mapper
public interface TrainingRecordMapper extends BaseMapper<TrainingRecordPO> {
}
