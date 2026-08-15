package com.ty.hikingalone.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.po.NavMenuPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导航菜单 Mapper
 */
@Mapper
public interface NavMenuMapper extends BaseMapper<NavMenuPO> {
}
