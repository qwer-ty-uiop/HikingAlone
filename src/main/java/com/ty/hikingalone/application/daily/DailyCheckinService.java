package com.ty.hikingalone.application.daily;

import com.ty.hikingalone.application.daily.cmd.DailyCheckinCmd;
import com.ty.hikingalone.domain.daily.entity.DailyCheckin;
import com.ty.hikingalone.domain.daily.repository.DailyCheckinRepository;
import com.ty.hikingalone.interfaces.daily.converter.DailyCheckinConverter;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyCheckinVO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyRecentVO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailySummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 每日打卡模块应用服务：部分覆盖 upsert 编排与 7 天均值统计（区间校验在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyCheckinService {

    /** recent 缺省查询天数 */
    private static final int DEFAULT_DAYS = 30;

    /** recent 查询天数下限 */
    private static final int MIN_DAYS = 7;

    /** recent 查询天数上限 */
    private static final int MAX_DAYS = 365;

    /** 均值统计窗口天数 */
    private static final int AVG_WINDOW_DAYS = 7;

    private final DailyCheckinRepository checkinRepository;
    private final DailyCheckinConverter converter;

    /**
     * 打卡（部分更新：只覆盖非 null 字段）
     * <p>先查后写；并发下首录同一天会撞 uk_user_date 唯一索引，捕获后转为对已存在记录的合并，保证幂等</p>
     */
    @Transactional
    public DailyCheckinVO checkin(Long userId, DailyCheckinCmd cmd) {
        if (cmd.getWaterCups() == null && cmd.getSteps() == null
                && cmd.getSleepHours() == null && cmd.getSleepQuality() == null) {
            throw new IllegalArgumentException("请至少填写一项打卡数据");
        }
        LocalDate date = resolveDate(cmd.getDate());
        DailyCheckin existing = checkinRepository.findByUserAndDate(userId, date);
        if (existing != null) {
            existing.merge(cmd.getWaterCups(), cmd.getSteps(), cmd.getSleepHours(), cmd.getSleepQuality());
            checkinRepository.save(existing);
            return converter.toVO(existing);
        }
        DailyCheckin checkin = DailyCheckin.record(userId, date,
                cmd.getWaterCups(), cmd.getSteps(), cmd.getSleepHours(), cmd.getSleepQuality());
        try {
            checkin.attachId(checkinRepository.save(checkin));
        } catch (DuplicateKeyException e) {
            // 并发首录同一天：唯一索引兜底，转为合并更新（读到的必是刚才并发插入的那条）
            DailyCheckin concurrent = checkinRepository.findByUserAndDate(userId, date);
            if (concurrent == null) {
                throw e;
            }
            concurrent.merge(cmd.getWaterCups(), cmd.getSteps(), cmd.getSleepHours(), cmd.getSleepQuality());
            checkinRepository.save(concurrent);
            return converter.toVO(concurrent);
        }
        return converter.toVO(checkin);
    }

    /**
     * 最近 N 天记录（升序）+ 汇总统计；days 缺省 30，钳制到 [7, 365]
     */
    public DailyRecentVO recent(Long userId, Integer days) {
        int d = days == null ? DEFAULT_DAYS : Math.max(MIN_DAYS, Math.min(MAX_DAYS, days));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(d - 1L);
        List<DailyCheckin> records = checkinRepository.listByUserAndDateRange(userId, start, end);
        return converter.toRecentVO(records, buildSummary(records), d);
    }

    /**
     * 汇总统计：区间记录天数 + 各指标 7 天均值（均值分母为该指标有值的天数，无值时 null）
     */
    private DailySummaryVO buildSummary(List<DailyCheckin> records) {
        DailySummaryVO summary = new DailySummaryVO();
        summary.setRecordedDays(records.size());
        summary.setAvgWater7(average(records, DailyCheckin::getWaterCups, 1));
        summary.setAvgSteps7(average(records, DailyCheckin::getSteps, 0));
        summary.setAvgSleep7(average(records, DailyCheckin::getSleepHours, 1));
        return summary;
    }

    /**
     * 指标均值：只统计窗口内（最后 7 天）有值的天数，无值返回 null
     */
    private BigDecimal average(List<DailyCheckin> records,
                               java.util.function.Function<DailyCheckin, Number> getter, int scale) {
        List<Number> values = records.stream()
                .skip(Math.max(0, records.size() - AVG_WINDOW_DAYS))
                .map(getter)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream()
                .map(v -> new BigDecimal(v.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(values.size()), scale, RoundingMode.HALF_UP);
    }

    /**
     * 解析记录日期：空=今天；格式错误由 GlobalExceptionHandler 统一转 400
     */
    private LocalDate resolveDate(String date) {
        return date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
    }
}
