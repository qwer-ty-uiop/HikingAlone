package com.ty.hikingalone.application.journal;

import com.ty.hikingalone.application.journal.cmd.JournalCmd;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.interfaces.journal.converter.JournalConverter;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalDetailVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalSummaryVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 徒步日记模块应用服务：日期解析、轨迹归属校验、缺失指标自动带出与跨记录汇总
 * （单条记述的不变量在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JournalService {

    /** 列表缺省条数 */
    private static final int DEFAULT_LIST_LIMIT = 50;

    /** 列表条数上下限 */
    private static final int MIN_LIST_LIMIT = 1;
    private static final int MAX_LIST_LIMIT = 200;

    private final HikeJournalRepository journalRepository;
    private final RouteTrackRepository routeTrackRepository;
    private final JournalConverter converter;

    /**
     * 新建日记：校验关联轨迹归属，未填的里程/时长/爬升从轨迹带出
     */
    @Transactional
    public JournalVO create(Long userId, JournalCmd cmd) {
        fillFromRoute(userId, cmd);
        HikeJournal journal = HikeJournal.record(userId, cmd.getTitle(), parseDate(cmd.getHikeDate()),
                cmd.getRouteId(), cmd.getDistanceKm(), cmd.getDurationMin(), cmd.getElevationGain(),
                cmd.getDifficulty(), cmd.getMood(), cmd.getWeather(), cmd.getCompanions(),
                cmd.getCost(), cmd.getContent());
        journal.attachId(journalRepository.save(journal));
        return converter.toVO(journal, routeNameMap(journal.getRouteId()));
    }

    /**
     * 编辑日记：归属校验 + 轨迹带出规则同新建
     */
    @Transactional
    public JournalVO update(Long userId, JournalCmd cmd) {
        HikeJournal existing = requireOwnedJournal(userId, cmd.getId());
        fillFromRoute(userId, cmd);
        existing.update(cmd.getTitle(), parseDate(cmd.getHikeDate()), cmd.getRouteId(),
                cmd.getDistanceKm(), cmd.getDurationMin(), cmd.getElevationGain(),
                cmd.getDifficulty(), cmd.getMood(), cmd.getWeather(), cmd.getCompanions(),
                cmd.getCost(), cmd.getContent());
        journalRepository.save(existing);
        return converter.toVO(existing, routeNameMap(existing.getRouteId()));
    }

    /**
     * 删除日记（物理删除；不影响关联轨迹）
     */
    @Transactional
    public void delete(Long userId, Long id) {
        requireOwnedJournal(userId, id);
        journalRepository.delete(id);
    }

    /**
     * 最近日记列表（limit 钳制 [1, 200]，缺省 50）
     */
    public List<JournalVO> listRecent(Long userId, Integer limit) {
        int l = limit == null ? DEFAULT_LIST_LIMIT : Math.max(MIN_LIST_LIMIT, Math.min(MAX_LIST_LIMIT, limit));
        List<HikeJournal> journals = journalRepository.listRecent(userId, l);
        return converter.toVOList(journals, routeNameMap(journals));
    }

    /**
     * 日记详情（完整正文）
     */
    public JournalDetailVO detail(Long userId, Long id) {
        HikeJournal journal = requireOwnedJournal(userId, id);
        return converter.toDetailVO(journal, routeNameMap(journal));
    }

    /**
     * 汇总统计：全量聚合（个人量级直接内存计算）
     */
    public JournalSummaryVO summary(Long userId) {
        List<HikeJournal> journals = journalRepository.listByUser(userId);
        JournalSummaryVO summary = new JournalSummaryVO();
        summary.setTotalCount(journals.size());
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalElevation = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal maxSingleDistance = null;
        int thisYear = LocalDate.now().getYear();
        int thisYearCount = 0;
        BigDecimal thisYearDistance = BigDecimal.ZERO;
        long totalDuration = 0;
        for (HikeJournal j : journals) {
            if (j.getDistanceKm() != null) {
                totalDistance = totalDistance.add(j.getDistanceKm());
                if (maxSingleDistance == null || j.getDistanceKm().compareTo(maxSingleDistance) > 0) {
                    maxSingleDistance = j.getDistanceKm();
                }
                if (j.getHikeDate().getYear() == thisYear) {
                    thisYearDistance = thisYearDistance.add(j.getDistanceKm());
                }
            }
            if (j.getElevationGain() != null) {
                totalElevation = totalElevation.add(j.getElevationGain());
            }
            if (j.getCost() != null) {
                totalCost = totalCost.add(j.getCost());
            }
            if (j.getDurationMin() != null) {
                totalDuration += j.getDurationMin();
            }
            if (j.getHikeDate().getYear() == thisYear) {
                thisYearCount++;
            }
        }
        summary.setTotalDistanceKm(totalDistance);
        summary.setTotalDurationMin((int) totalDuration);
        summary.setTotalElevationGain(totalElevation);
        summary.setTotalCost(totalCost);
        summary.setThisYearCount(thisYearCount);
        summary.setThisYearDistanceKm(thisYearDistance);
        summary.setMaxSingleDistanceKm(maxSingleDistance);
        summary.setLastHikeDate(journals.isEmpty() ? null : journals.get(0).getHikeDate().toString());
        return summary;
    }

    /**
     * 归属校验：日记不存在或不属于当前用户统一报「日记不存在」（防越权探测）
     */
    private HikeJournal requireOwnedJournal(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("日记 id 不能为空");
        }
        HikeJournal journal = journalRepository.findById(id);
        if (journal == null || !userId.equals(journal.getUserId())) {
            throw new IllegalArgumentException("日记不存在");
        }
        return journal;
    }

    /**
     * 跨实体规则：关联轨迹必须属于当前用户；里程/时长/爬升未填时从轨迹带出（显式传入的值不覆盖）
     */
    private void fillFromRoute(Long userId, JournalCmd cmd) {
        if (cmd.getRouteId() == null) {
            return;
        }
        RouteTrack route = routeTrackRepository.findById(cmd.getRouteId());
        if (route == null || !userId.equals(route.getUserId())) {
            throw new IllegalArgumentException("关联的路线不存在");
        }
        if (cmd.getDistanceKm() == null) {
            cmd.setDistanceKm(route.getDistance());
        }
        if (cmd.getDurationMin() == null) {
            cmd.setDurationMin(route.getDurationMin());
        }
        if (cmd.getElevationGain() == null) {
            cmd.setElevationGain(route.getElevationGain());
        }
    }

    /**
     * 解析徒步日期：格式错误由 GlobalExceptionHandler 统一转 400
     */
    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("徒步日期不能为空");
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("徒步日期格式错误，应为 yyyy-MM-dd");
        }
    }

    /**
     * 批量补齐 routeName（列表页展示；轨迹被删后为 null，不级联清理）
     */
    private Map<Long, String> routeNameMap(List<HikeJournal> journals) {
        Map<Long, String> names = new HashMap<>();
        for (HikeJournal j : journals) {
            collectRouteName(names, j.getRouteId());
        }
        return names;
    }

    private Map<Long, String> routeNameMap(HikeJournal journal) {
        Map<Long, String> names = new HashMap<>();
        collectRouteName(names, journal.getRouteId());
        return names;
    }

    private Map<Long, String> routeNameMap(Long routeId) {
        Map<Long, String> names = new HashMap<>();
        collectRouteName(names, routeId);
        return names;
    }

    private void collectRouteName(Map<Long, String> names, Long routeId) {
        if (routeId == null || names.containsKey(routeId)) {
            return;
        }
        RouteTrack route = routeTrackRepository.findById(routeId);
        names.put(routeId, route == null ? null : route.getName());
    }
}
