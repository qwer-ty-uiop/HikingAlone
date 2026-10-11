package com.ty.hikingalone.infrastructure.gear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 装备台账 Mapper
 */
@Mapper
public interface GearItemMapper extends BaseMapper<GearItemPO> {
}
