package com.ty.hikingalone.domain.daily.repository;

import com.ty.hikingalone.domain.daily.entity.DailyCheckin;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日打卡仓储接口（领域层定义语义，实现收敛在基础设施层）
 */
public interface DailyCheckinRepository {

    /**
     * 查某用户某天的打卡（upsert 前置查询；每天至多一条）
     */
    DailyCheckin findByUserAndDate(Long userId, LocalDate recordDate);

    /**
     * 查某用户日期区间内的打卡，按日期升序（趋势图/列表直接可用）
     */
    List<DailyCheckin> listByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(DailyCheckin checkin);
}
