package com.ty.hikingalone.application.route;

import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.interfaces.route.converter.RouteConverter;
import com.ty.hikingalone.interfaces.route.vo.query.RouteHotVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 常走路线应用服务（GET /routes/hot）：跨模块只读聚合（同成就徽章先例）。
 * <p>单用户系统里「热门」= 我常走的路线：按 hike_journal.route_id 引用次数排序，
 * 回答「哪条线我最常走、最近一次什么时候走的」，并服务「再走一次」的复购式动线</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteHotService {

    /** 缺省返回条数 */
    private static final int DEFAULT_LIMIT = 5;

    /** 条数下限 */
    private static final int MIN_LIMIT = 1;

    /** 条数上限 */
    private static final int MAX_LIMIT = 20;

    private final RouteTrackRepository routeTrackRepository;
    private final HikeJournalRepository journalRepository;
    private final RouteConverter converter;

    /**
     * 常走路线 Top N：hikeCount 降序 → lastHikeDate 降序；只含被日记引用过的路线
     */
    @Transactional(readOnly = true)
    public List<RouteHotVO> hot(Long userId, Integer limit) {
        int l = limit == null ? DEFAULT_LIMIT : Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, limit));

        List<HikeJournal> journals = journalRepository.listByUser(userId);
        Map<Long, List<HikeJournal>> journalsByRoute = journals.stream()
                .filter(j -> j.getRouteId() != null)
                .collect(Collectors.groupingBy(HikeJournal::getRouteId));
        if (journalsByRoute.isEmpty()) {
            return List.of();
        }
        Map<Long, RouteTrack> routeById = routeTrackRepository.listByUserId(userId).stream()
                .collect(Collectors.toMap(RouteTrack::getId, Function.identity()));

        return journalsByRoute.entrySet().stream()
                .map(entry -> {
                    RouteTrack route = routeById.get(entry.getKey());
                    // 轨迹已被删除：日记悬空引用不计入热门
                    return route == null ? null
                            : toHotVO(route, entry.getValue());
                })
                .filter(vo -> vo != null)
                .sorted(Comparator.comparingInt(RouteHotVO::getHikeCount).reversed()
                        .thenComparing(Comparator.comparing(RouteHotVO::getLastHikeDate).reversed()))
                .limit(l)
                .toList();
    }

    /**
     * 路线 + 该路线的日记列表 → 热 VO（次数/最近/最早徒步日期）
     */
    private RouteHotVO toHotVO(RouteTrack route, List<HikeJournal> journals) {
        List<LocalDate> dates = journals.stream()
                .map(HikeJournal::getHikeDate)
                .filter(d -> d != null)
                .toList();
        LocalDate first = dates.stream().min(Comparator.naturalOrder()).orElse(null);
        LocalDate last = dates.stream().max(Comparator.naturalOrder()).orElse(null);
        return converter.toHotVO(route, journals.size(), last, first);
    }
}
