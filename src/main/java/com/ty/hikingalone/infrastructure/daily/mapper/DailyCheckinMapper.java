package com.ty.hikingalone.infrastructure.daily.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.daily.po.DailyCheckinPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日打卡 Mapper
 */
@Mapper
public interface DailyCheckinMapper extends BaseMapper<DailyCheckinPO> {
}
