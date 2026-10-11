package com.ty.hikingalone.infrastructure.gear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearChecklistPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打包清单 Mapper
 */
@Mapper
public interface GearChecklistMapper extends BaseMapper<GearChecklistPO> {
}
