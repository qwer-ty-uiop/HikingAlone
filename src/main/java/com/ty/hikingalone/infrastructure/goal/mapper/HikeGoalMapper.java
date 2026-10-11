package com.ty.hikingalone.infrastructure.goal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.goal.po.HikeGoalPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 徒步目标 Mapper
 */
@Mapper
public interface HikeGoalMapper extends BaseMapper<HikeGoalPO> {
}
