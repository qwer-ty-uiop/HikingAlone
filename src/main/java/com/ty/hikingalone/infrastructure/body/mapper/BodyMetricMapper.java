package com.ty.hikingalone.infrastructure.body.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.body.po.BodyMetricPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日身体数据 Mapper
 */
@Mapper
public interface BodyMetricMapper extends BaseMapper<BodyMetricPO> {
}
