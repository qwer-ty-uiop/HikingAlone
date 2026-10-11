package com.ty.hikingalone.application.body;

import com.ty.hikingalone.application.body.cmd.BodyMetricRecordCmd;
import com.ty.hikingalone.domain.body.entity.BodyMetric;
import com.ty.hikingalone.domain.body.repository.BodyMetricRepository;
import com.ty.hikingalone.interfaces.body.converter.BodyMetricConverter;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecentVO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecordVO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 身体数据模块应用服务：DTO 解析、upsert 流程编排与跨记录统计（趋势/连续天数），不含单条记录的不变量（在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BodyMetricService {

    /** recent 缺省查询天数 */
    private static final int DEFAULT_DAYS = 30;

    /** recent 查询天数下限 */
    private static final int MIN_DAYS = 7;

    /** recent 查询天数上限 */
    private static final int MAX_DAYS = 365;

    private final BodyMetricRepository bodyMetricRepository;
    private final BodyMetricConverter converter;

    /**
     * 记录/更新当日身体数据（每日一条，同日重复提交覆盖更新）
     * <p>先查后写；并发下首录同一天会撞 uk_user_date 唯一索引，捕获后转为对已存在记录的更新，保证幂等</p>
     */
    @Transactional
    public BodyMetricRecordVO record(Long userId, BodyMetricRecordCmd cmd) {
        LocalDate date = resolveDate(cmd.getDate());
        BodyMetric existing = bodyMetricRepository.findByUserAndDate(userId, date);
        if (existing != null) {
            existing.update(cmd.getWeightKg(), cmd.getRestingHeartRate(), cmd.getNote());
            bodyMetricRepository.save(existing);
            return converter.toRecordVO(existing);
        }
        BodyMetric metric = BodyMetric.record(userId, date, cmd.getWeightKg(), cmd.getRestingHeartRate(), cmd.getNote());
        try {
            metric.attachId(bodyMetricRepository.save(metric));
        } catch (DuplicateKeyException e) {
            // 并发首录同一天：唯一索引兜底，转为覆盖更新（读到的必是刚才并发插入的那条）
            BodyMetric concurrent = bodyMetricRepository.findByUserAndDate(userId, date);
            if (concurrent == null) {
                throw e;
            }
            concurrent.update(cmd.getWeightKg(), cmd.getRestingHeartRate(), cmd.getNote());
            bodyMetricRepository.save(concurrent);
            return converter.toRecordVO(concurrent);
        }
        return converter.toRecordVO(metric);
    }

    /**
     * 最近 N 天记录（升序）+ 汇总统计；days 缺省 30，钳制到 [7, 365]
     */
    public BodyMetricRecentVO recent(Long userId, Integer days) {
        int d = days == null ? DEFAULT_DAYS : Math.max(MIN_DAYS, Math.min(MAX_DAYS, days));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(d - 1L);
        List<BodyMetric> records = bodyMetricRepository.listByUserAndDateRange(userId, start, end);
        return converter.toRecentVO(records, buildSummary(records), d);
    }

    /**
     * 汇总统计：最新/上一次体重与差值、区间记录天数、连续记录天数。
     * <p>跨多条记录的聚合语义，放应用服务而非实体（实体无状态）</p>
     */
    private BodyMetricSummaryVO buildSummary(List<BodyMetric> records) {
        BodyMetricSummaryVO summary = new BodyMetricSummaryVO();
        if (records.isEmpty()) {
            return summary;
        }
        BodyMetric latest = records.get(records.size() - 1);
        summary.setLatestWeight(latest.getWeightKg());
        if (records.size() >= 2) {
            BigDecimal prev = records.get(records.size() - 2).getWeightKg();
            summary.setPrevWeight(prev);
            summary.setWeightDelta(latest.getWeightKg().subtract(prev).setScale(1, RoundingMode.HALF_UP));
        }
        summary.setRecordedDays(records.size());
        summary.setStreakDays(calcStreakDays(records));
        return summary;
    }

    /**
     * 连续记录天数：从今天往回数连续有记录的天数；今天未记录则从昨天起算（保护「晚上才称重」的连续性）
     */
    private int calcStreakDays(List<BodyMetric> records) {
        Set<LocalDate> dates = new HashSet<>();
        for (BodyMetric r : records) {
            dates.add(r.getRecordDate());
        }
        LocalDate cursor = LocalDate.now();
        if (!dates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * 解析记录日期：空=今天；格式错误由 GlobalExceptionHandler 统一转 400
     */
    private LocalDate resolveDate(String date) {
        return date == null || date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
    }
}
