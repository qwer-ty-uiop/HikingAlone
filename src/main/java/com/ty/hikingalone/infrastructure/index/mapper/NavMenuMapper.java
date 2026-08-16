package com.ty.hikingalone.infrastructure.index.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.index.po.NavMenuPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导航菜单 Mapper
 */
@Mapper
public interface NavMenuMapper extends BaseMapper<NavMenuPO> {
}
