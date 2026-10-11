package com.ty.hikingalone.infrastructure.gear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearChecklistItemPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打包清单条目 Mapper
 */
@Mapper
public interface GearChecklistItemMapper extends BaseMapper<GearChecklistItemPO> {
}
