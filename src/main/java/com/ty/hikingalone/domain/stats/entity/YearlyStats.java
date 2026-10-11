package com.ty.hikingalone.domain.stats.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 年度统计值对象：由应用服务从日记聚合组装，月度分布固定 1~12 月
 */
public record YearlyStats(
        int year,
        int hikeCount,
        BigDecimal totalDistanceKm,
        int totalDurationMin,
        BigDecimal totalElevationGain,
        BigDecimal totalCost,
        BigDecimal maxSingleDistanceKm,
        BigDecimal maxSingleElevationGain,
        LocalDate firstHikeDate,
        List<MonthlyPoint> monthly) {

    /**
     * 月度分布点（月 / 次数 / 里程）
     */
    public record MonthlyPoint(int month, int hikeCount, BigDecimal distanceKm) {
    }

    /**
     * 从某年日记聚合年度统计（monthly 固定 12 个月，无徒步的月补零）
     */
    public static YearlyStats assemble(int year, List<com.ty.hikingalone.domain.journal.entity.HikeJournal> journals) {
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalElevation = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal maxDistance = null;
        BigDecimal maxElevation = null;
        LocalDate firstHike = null;
        long totalDuration = 0;
        int[] monthCounts = new int[12];
        BigDecimal[] monthDistances = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            monthDistances[i] = BigDecimal.ZERO;
        }
        for (var j : journals) {
            if (j.getDistanceKm() != null) {
                totalDistance = totalDistance.add(j.getDistanceKm());
                monthDistances[j.getHikeDate().getMonthValue() - 1] =
                        monthDistances[j.getHikeDate().getMonthValue() - 1].add(j.getDistanceKm());
                if (maxDistance == null || j.getDistanceKm().compareTo(maxDistance) > 0) {
                    maxDistance = j.getDistanceKm();
                }
            }
            if (j.getElevationGain() != null) {
                totalElevation = totalElevation.add(j.getElevationGain());
                if (maxElevation == null || j.getElevationGain().compareTo(maxElevation) > 0) {
                    maxElevation = j.getElevationGain();
                }
            }
            if (j.getCost() != null) {
                totalCost = totalCost.add(j.getCost());
            }
            if (j.getDurationMin() != null) {
                totalDuration += j.getDurationMin();
            }
            monthCounts[j.getHikeDate().getMonthValue() - 1]++;
            if (firstHike == null || j.getHikeDate().isBefore(firstHike)) {
                firstHike = j.getHikeDate();
            }
        }
        List<MonthlyPoint> monthly = new java.util.ArrayList<>(12);
        for (int m = 1; m <= 12; m++) {
            monthly.add(new MonthlyPoint(m, monthCounts[m - 1], monthDistances[m - 1]));
        }
        return new YearlyStats(year, journals.size(), totalDistance, (int) totalDuration,
                totalElevation, totalCost, maxDistance, maxElevation, firstHike,
                java.util.List.copyOf(monthly));
    }
}
