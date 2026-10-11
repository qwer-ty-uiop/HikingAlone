package com.ty.hikingalone.infrastructure.gear.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.gear.po.GearCatalogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 常用装备目录 Mapper
 */
@Mapper
public interface GearCatalogMapper extends BaseMapper<GearCatalogPO> {
}
