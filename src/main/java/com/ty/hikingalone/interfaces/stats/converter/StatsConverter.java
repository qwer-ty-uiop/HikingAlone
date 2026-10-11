package com.ty.hikingalone.interfaces.stats.converter;

import com.ty.hikingalone.domain.stats.entity.YearlyStats;
import com.ty.hikingalone.interfaces.stats.vo.query.AvailableYearVO;
import com.ty.hikingalone.interfaces.stats.vo.query.MonthlyPointVO;
import com.ty.hikingalone.interfaces.stats.vo.query.YearlyStatsVO;
import org.springframework.stereotype.Component;

/**
 * 年度统计 值对象 → VO 转换器
 */
@Component
public class StatsConverter {

    public YearlyStatsVO toVO(YearlyStats stats) {
        YearlyStatsVO vo = new YearlyStatsVO();
        vo.setYear(stats.year());
        vo.setHikeCount(stats.hikeCount());
        vo.setTotalDistanceKm(stats.totalDistanceKm());
        vo.setTotalDurationMin(stats.totalDurationMin());
        vo.setTotalElevationGain(stats.totalElevationGain());
        vo.setTotalCost(stats.totalCost());
        vo.setMaxSingleDistanceKm(stats.maxSingleDistanceKm());
        vo.setMaxSingleElevationGain(stats.maxSingleElevationGain());
        vo.setFirstHikeDate(stats.firstHikeDate() == null ? null : stats.firstHikeDate().toString());
        vo.setMonthly(stats.monthly().stream().map(p -> {
            MonthlyPointVO point = new MonthlyPointVO();
            point.setMonth(p.month());
            point.setHikeCount(p.hikeCount());
            point.setDistanceKm(p.distanceKm());
            return point;
        }).toList());
        return vo;
    }

    public AvailableYearVO toYearVO(int year, int hikeCount) {
        AvailableYearVO vo = new AvailableYearVO();
        vo.setYear(year);
        vo.setHikeCount(hikeCount);
        return vo;
    }
}
