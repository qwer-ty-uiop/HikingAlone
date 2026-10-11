package com.ty.hikingalone.domain.body.repository;

import com.ty.hikingalone.domain.body.entity.BodyMetric;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日身体数据仓储接口（领域层定义语义，实现收敛在基础设施层）
 */
public interface BodyMetricRepository {

    /**
     * 查某用户某天的身体数据（upsert 前置查询；每天至多一条）
     */
    BodyMetric findByUserAndDate(Long userId, LocalDate recordDate);

    /**
     * 查某用户日期区间内的记录，按日期升序（趋势图/列表直接可用）
     */
    List<BodyMetric> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(BodyMetric metric);
}
