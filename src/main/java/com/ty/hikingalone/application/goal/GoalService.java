package com.ty.hikingalone.application.goal;

import com.ty.hikingalone.application.goal.cmd.GoalCreateCmd;
import com.ty.hikingalone.application.goal.cmd.GoalIdCmd;
import com.ty.hikingalone.application.goal.cmd.GoalUpdateCmd;
import com.ty.hikingalone.common.enums.goal.GoalMetricEnum;
import com.ty.hikingalone.common.enums.goal.GoalPeriodTypeEnum;
import com.ty.hikingalone.domain.goal.entity.HikeGoal;
import com.ty.hikingalone.domain.goal.repository.HikeGoalRepository;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.interfaces.goal.converter.GoalConverter;
import com.ty.hikingalone.interfaces.goal.vo.query.GoalVO;
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
 * 目标应用服务：查重编排、跨模块聚合日记进度（读时计算，不落库）
 * （目标值区间等不变量在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    /** 进度百分比上限（超额显示真实倍数，钳制防溢出） */
    private static final int MAX_PERCENT = 999;

    /** 年份参数下限 */
    private static final int MIN_YEAR = 2020;

    /** 年份参数上限 */
    private static final int MAX_YEAR = 2100;

    private final HikeGoalRepository goalRepository;
    private final HikeJournalRepository journalRepository;
    private final GoalConverter converter;

    /**
     * 新建目标：同期同指标查重（应用层先查 + DB 唯一索引兜底）
     */
    @Transactional
    public GoalVO create(Long userId, GoalCreateCmd cmd) {
        HikeGoal goal = HikeGoal.create(userId, cmd.getMetric(), cmd.getTargetValue(),
                cmd.getYear(), cmd.getMonth());
        GoalMetricEnum metricEnum = GoalMetricEnum.fromCode(goal.getMetric());
        HikeGoal existing = goalRepository.findByPeriodAndMetric(userId, goal.getYear(), goal.getMonth(), goal.getMetric());
        if (existing != null) {
            throw new IllegalArgumentException("该周期已存在同类型目标");
        }
        try {
            goal.attachId(goalRepository.save(goal));
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("该周期已存在同类型目标");
        }
        return converter.toVO(goal, buildProgress(goal, metricEnum,
                journalRepository.listByUserAndYear(userId, goal.getYear())));
    }

    /**
     * 编辑目标（只许改 target/status）
     */
    @Transactional
    public GoalVO update(Long userId, GoalUpdateCmd cmd) {
        HikeGoal existing = requireOwnedGoal(userId, cmd.getId());
        existing.update(cmd.getTargetValue(), cmd.getStatus());
        goalRepository.save(existing);
        GoalMetricEnum metricEnum = GoalMetricEnum.fromCode(existing.getMetric());
        return converter.toVO(existing, buildProgress(existing, metricEnum,
                journalRepository.listByUserAndYear(userId, existing.getYear())));
    }

    /**
     * 删除目标
     */
    @Transactional
    public void delete(Long userId, GoalIdCmd cmd) {
        requireOwnedGoal(userId, cmd.getId());
        goalRepository.delete(cmd.getId());
    }

    /**
     * 某年目标列表（含读时进度）；year 缺省当前年，钳制 [2020, 2100]
     */
    public List<GoalVO> list(Long userId, Integer year) {
        int y = year == null ? LocalDate.now().getYear() : Math.max(MIN_YEAR, Math.min(MAX_YEAR, year));
        List<HikeGoal> goals = goalRepository.listByUserAndYear(userId, y);
        List<HikeJournal> yearJournals = journalRepository.listByUserAndYear(userId, y);
        return goals.stream()
                .map(g -> converter.toVO(g, buildProgress(g, GoalMetricEnum.fromCode(g.getMetric()), yearJournals)))
                .toList();
    }

    /**
     * 读时进度：按指标从当年日记聚合周期内的 current，再算 percent/achieved
     */
    private GoalVO.Progress buildProgress(HikeGoal goal, GoalMetricEnum metricEnum, List<HikeJournal> yearJournals) {
        LocalDate start;
        LocalDate end;
        if (GoalPeriodTypeEnum.MONTHLY.getCode().equals(goal.getPeriodType())) {
            start = LocalDate.of(goal.getYear(), goal.getMonth(), 1);
            end = start.plusMonths(1).minusDays(1);
        } else {
            start = LocalDate.of(goal.getYear(), 1, 1);
            end = LocalDate.of(goal.getYear(), 12, 31);
        }
        BigDecimal current;
        switch (metricEnum) {
            case MONTHLY_DISTANCE, YEARLY_DISTANCE -> current = sumDistance(yearJournals, start, end);
            case MONTHLY_HIKES, YEARLY_HIKES -> current = BigDecimal.valueOf(
                    yearJournals.stream().filter(j -> inPeriod(j.getHikeDate(), start, end)).count());
            case MONTHLY_ELEVATION, YEARLY_ELEVATION -> current = sumElevation(yearJournals, start, end);
            default -> current = BigDecimal.ZERO;
        }
        int percent;
        if (goal.getTargetValue().compareTo(BigDecimal.ZERO) <= 0) {
            percent = 0;
        } else {
            percent = current.multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetValue(), 0, RoundingMode.DOWN)
                    .intValue();
            percent = Math.max(0, Math.min(MAX_PERCENT, percent));
        }
        GoalVO.Progress progress = new GoalVO.Progress();
        progress.setCurrent(current);
        progress.setPercent(percent);
        progress.setAchieved(current.compareTo(goal.getTargetValue()) >= 0);
        progress.setPeriodEnded(end.isBefore(LocalDate.now()));
        progress.setUnit(metricEnum.getUnit());
        return progress;
    }

    /**
     * 周期内日记里程求和（null 记 0）
     */
    private BigDecimal sumDistance(List<HikeJournal> journals, LocalDate start, LocalDate end) {
        return journals.stream()
                .filter(j -> inPeriod(j.getHikeDate(), start, end))
                .map(HikeJournal::getDistanceKm)
                .filter(d -> d != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 周期内日记爬升求和（null 记 0）
     */
    private BigDecimal sumElevation(List<HikeJournal> journals, LocalDate start, LocalDate end) {
        return journals.stream()
                .filter(j -> inPeriod(j.getHikeDate(), start, end))
                .map(HikeJournal::getElevationGain)
                .filter(e -> e != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean inPeriod(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * 归属校验：不存在或不属于当前用户统一报「目标不存在」
     */
    private HikeGoal requireOwnedGoal(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("目标 id 不能为空");
        }
        HikeGoal goal = goalRepository.findById(id);
        if (goal == null || !userId.equals(goal.getUserId())) {
            throw new IllegalArgumentException("目标不存在");
        }
        return goal;
    }
}
