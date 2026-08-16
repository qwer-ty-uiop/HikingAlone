package com.ty.hikingalone.infrastructure.index.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.index.po.HomeBannerPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页横幅 Mapper
 */
@Mapper
public interface HomeBannerMapper extends BaseMapper<HomeBannerPO> {
}
