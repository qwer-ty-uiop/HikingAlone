package com.ty.hikingalone.infrastructure.route.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.route.po.RouteTrackPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 路线轨迹 Mapper
 */
@Mapper
public interface RouteTrackMapper extends BaseMapper<RouteTrackPO> {
}
