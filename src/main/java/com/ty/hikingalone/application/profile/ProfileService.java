package com.ty.hikingalone.application.profile;

import com.ty.hikingalone.application.achievement.AchievementService;
import com.ty.hikingalone.application.goal.GoalService;
import com.ty.hikingalone.application.journal.JournalService;
import com.ty.hikingalone.application.wish.WishService;
import com.ty.hikingalone.common.enums.goal.GoalPeriodTypeEnum;
import com.ty.hikingalone.domain.body.repository.BodyMetricRepository;
import com.ty.hikingalone.domain.daily.entity.DailyCheckin;
import com.ty.hikingalone.domain.daily.repository.DailyCheckinRepository;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.interfaces.achievement.vo.query.AchievementGroupVO;
import com.ty.hikingalone.interfaces.goal.vo.query.GoalVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalSummaryVO;
import com.ty.hikingalone.interfaces.profile.vo.query.DashboardVO;
import com.ty.hikingalone.interfaces.wish.vo.query.WishSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人中心驾驶舱应用服务：零表读时聚合 + 待办规则引擎
 * <p>把分散在各模块的数据聚成一屏（今日打卡/概览/连续/提醒），
 * 把「用户到处手动填表」变成「系统提醒 + 一键完成」。聚合均复用各域应用服务，不穿透他域内部表</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    /** 待办类型：轨迹未写日记 */
    private static final String TODO_JOURNAL_PENDING = "JOURNAL_PENDING";

    /** 待办类型：目标进度落后 */
    private static final String TODO_GOAL_BEHIND = "GOAL_BEHIND";

    /** 「新轨迹」判定窗口（上传后 7 天内没写日记才提醒，避免陈年轨迹永久挂提醒） */
    private static final int RECENT_ROUTE_DAYS = 7;

    /** 最近日记条数 */
    private static final int RECENT_JOURNAL_COUNT = 3;

    /** 进行中目标展示上限 */
    private static final int ACTIVE_GOAL_LIMIT = 4;

    /** 身体数据连续天数统计窗口 */
    private static final int BODY_STREAK_WINDOW_DAYS = 365;

    private final JournalService journalService;
    private final GoalService goalService;
    private final WishService wishService;
    private final AchievementService achievementService;

    private final DailyCheckinRepository dailyCheckinRepository;
    private final HikeJournalRepository journalRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final RouteTrackRepository routeTrackRepository;

    /**
     * 驾驶舱聚合（只读，无写副作用）
     */
    @Transactional(readOnly = true)
    public DashboardVO dashboard(Long userId) {
        DashboardVO vo = new DashboardVO();
        vo.setToday(buildToday(userId));
        vo.setTodos(buildTodos(userId));
        vo.setHiking(buildHiking(userId));
        vo.setStreaks(buildStreaks(userId));
        vo.setRecentJournals(buildRecentJournals(userId));
        vo.setActiveGoals(buildActiveGoals(userId));
        vo.setWish(buildWish(userId));
        vo.setAchievement(buildAchievement(userId));
        return vo;
    }

    /**
     * 今日打卡：当天 daily_checkin 的部分字段（未打卡 checkedIn=false、值 null）
     */
    private DashboardVO.Today buildToday(Long userId) {
        DashboardVO.Today today = new DashboardVO.Today();
        DailyCheckin checkin = dailyCheckinRepository.findByUserAndDate(userId, LocalDate.now());
        if (checkin == null) {
            today.setCheckedIn(false);
            return today;
        }
        today.setWaterCups(checkin.getWaterCups());
        today.setSteps(checkin.getSteps());
        today.setSleepHours(checkin.getSleepHours());
        today.setSleepQuality(checkin.getSleepQuality());
        today.setCheckedIn(checkin.getWaterCups() != null || checkin.getSteps() != null
                || checkin.getSleepHours() != null || checkin.getSleepQuality() != null);
        return today;
    }

    /**
     * 待办规则引擎（v1 两条，规则集中于此便于演进）：
     * 1) 轨迹未写日记：7 天内上传的轨迹，至今没有任何日记引用它
     * 2) 目标进度落后：进行中、未达成、周期未结束，且进度百分比 < 日历时间进度
     */
    private List<DashboardVO.Todo> buildTodos(Long userId) {
        List<DashboardVO.Todo> todos = new ArrayList<>();

        // 规则 1：轨迹未写日记
        Set<Long> journaledRouteIds = journalRepository.listByUser(userId).stream()
                .map(HikeJournal::getRouteId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        long pendingRoutes = routeTrackRepository.listByUserId(userId).stream()
                .filter(r -> r.getCreateTime() != null
                        && r.getCreateTime().isAfter(java.time.LocalDateTime.now().minusDays(RECENT_ROUTE_DAYS))
                        && !journaledRouteIds.contains(r.getId()))
                .count();
        if (pendingRoutes > 0) {
            DashboardVO.Todo todo = new DashboardVO.Todo();
            todo.setType(TODO_JOURNAL_PENDING);
            todo.setMessage("有 " + pendingRoutes + " 条新轨迹还没写日记");
            todo.setTarget("/journal");
            todos.add(todo);
        }

        // 规则 2：目标进度落后
        for (GoalVO goal : goalService.list(userId, Year.now().getValue())) {
            if (!Integer.valueOf(1).equals(goal.getStatus())
                    || goal.getProgress() == null
                    || Boolean.TRUE.equals(goal.getProgress().getAchieved())
                    || Boolean.TRUE.equals(goal.getProgress().getPeriodEnded())) {
                continue;
            }
            int calendarPercent = calendarPercent(goal);
            if (goal.getProgress().getPercent() < calendarPercent) {
                DashboardVO.Todo todo = new DashboardVO.Todo();
                todo.setType(TODO_GOAL_BEHIND);
                todo.setMessage(goal.getMetricLabel() + "进度落后（" + goal.getProgress().getPercent()
                        + "%，时间已过 " + calendarPercent + "%）");
                todo.setTarget("/goal");
                todos.add(todo);
            }
        }
        return todos;
    }

    /**
     * 日历时间进度：月度 = 当月已过天数占比；年度 = 全年已过天数占比
     */
    private int calendarPercent(GoalVO goal) {
        LocalDate today = LocalDate.now();
        if (GoalPeriodTypeEnum.MONTHLY.getCode().equals(goal.getPeriodType())) {
            int daysInMonth = YearMonth.of(goal.getYear(), goal.getMonth()).lengthOfMonth();
            return (int) Math.round(100.0 * today.getDayOfMonth() / daysInMonth);
        }
        int daysInYear = Year.of(goal.getYear()).length();
        return (int) Math.round(100.0 * today.getDayOfYear() / daysInYear);
    }

    /**
     * 徒步概览（复用徒步日记汇总口径）
     */
    private DashboardVO.Hiking buildHiking(Long userId) {
        JournalSummaryVO summary = journalService.summary(userId);
        DashboardVO.Hiking hiking = new DashboardVO.Hiking();
        hiking.setTotalCount(summary.getTotalCount());
        hiking.setTotalDistanceKm(summary.getTotalDistanceKm());
        hiking.setTotalElevationGain(summary.getTotalElevationGain());
        hiking.setThisYearCount(summary.getThisYearCount());
        hiking.setThisYearDistanceKm(summary.getThisYearDistanceKm());
        return hiking;
    }

    /**
     * 连续记录：徒步日记按「年月」数连续月份数；身体数据按天连续（今天没记从昨天起算，与身体模块口径一致）
     */
    private DashboardVO.Streaks buildStreaks(Long userId) {
        DashboardVO.Streaks streaks = new DashboardVO.Streaks();

        Set<YearMonth> journalMonths = journalRepository.listByUser(userId).stream()
                .map(HikeJournal::getHikeDate)
                .filter(d -> d != null)
                .map(YearMonth::from)
                .collect(Collectors.toSet());
        int monthStreak = 0;
        YearMonth monthCursor = YearMonth.now();
        while (journalMonths.contains(monthCursor)) {
            monthStreak++;
            monthCursor = monthCursor.minusMonths(1);
        }
        streaks.setJournalStreakMonths(monthStreak);

        LocalDate bodyEnd = LocalDate.now();
        LocalDate bodyStart = bodyEnd.minusDays(BODY_STREAK_WINDOW_DAYS - 1L);
        Set<LocalDate> bodyDates = bodyMetricRepository
                .listByUserAndDateRange(userId, bodyStart, bodyEnd).stream()
                .map(m -> m.getRecordDate())
                .collect(Collectors.toSet());
        int dayStreak = 0;
        LocalDate dayCursor = bodyEnd;
        if (!bodyDates.contains(dayCursor)) {
            dayCursor = dayCursor.minusDays(1);
        }
        while (bodyDates.contains(dayCursor)) {
            dayStreak++;
            dayCursor = dayCursor.minusDays(1);
        }
        streaks.setBodyStreakDays(dayStreak);
        return streaks;
    }

    /**
     * 最近日记（≤3 篇）
     */
    private List<DashboardVO.JournalItem> buildRecentJournals(Long userId) {
        return journalRepository.listRecent(userId, RECENT_JOURNAL_COUNT).stream()
                .map(j -> {
                    DashboardVO.JournalItem item = new DashboardVO.JournalItem();
                    item.setId(j.getId());
                    item.setTitle(j.getTitle());
                    item.setHikeDate(j.getHikeDate().toString());
                    item.setDistanceKm(j.getDistanceKm());
                    return item;
                })
                .toList();
    }

    /**
     * 进行中目标（复用目标模块读时进度；当年范围，最多 4 个）
     */
    private List<DashboardVO.GoalItem> buildActiveGoals(Long userId) {
        return goalService.list(userId, Year.now().getValue()).stream()
                .filter(g -> Integer.valueOf(1).equals(g.getStatus()))
                .limit(ACTIVE_GOAL_LIMIT)
                .map(g -> {
                    DashboardVO.GoalItem item = new DashboardVO.GoalItem();
                    item.setId(g.getId());
                    item.setMetricLabel(g.getMetricLabel());
                    item.setTargetValue(g.getTargetValue());
                    item.setCurrent(g.getProgress() == null ? BigDecimal.ZERO : g.getProgress().getCurrent());
                    item.setPercent(g.getProgress() == null ? 0 : g.getProgress().getPercent());
                    item.setUnit(g.getProgress() == null ? "" : g.getProgress().getUnit());
                    return item;
                })
                .toList();
    }

    /**
     * 心愿概览
     */
    private DashboardVO.Wish buildWish(Long userId) {
        WishSummaryVO summary = wishService.summary(userId);
        DashboardVO.Wish wish = new DashboardVO.Wish();
        wish.setWishCount(summary.getWishCount());
        wish.setPlannedCount(summary.getPlannedCount());
        wish.setDoneCount(summary.getDoneCount());
        return wish;
    }

    /**
     * 徽章概览（复用成就目录实时计算）
     */
    private DashboardVO.Achievement buildAchievement(Long userId) {
        List<AchievementGroupVO> groups = achievementService.all(userId);
        int total = groups.stream().mapToInt(g -> g.getAchievements().size()).sum();
        int achieved = (int) groups.stream()
                .flatMap(g -> g.getAchievements().stream())
                .filter(a -> Boolean.TRUE.equals(a.getAchieved()))
                .count();
        DashboardVO.Achievement achievement = new DashboardVO.Achievement();
        achievement.setAchievedCount(achieved);
        achievement.setTotalCount(total);
        return achievement;
    }
}
