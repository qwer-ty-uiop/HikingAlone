package com.ty.hikingalone.infrastructure.wish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.wish.po.HikeWishPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 徒步心愿 Mapper
 */
@Mapper
public interface HikeWishMapper extends BaseMapper<HikeWishPO> {
}
