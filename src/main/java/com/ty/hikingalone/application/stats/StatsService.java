package com.ty.hikingalone.application.stats;

import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.stats.entity.YearlyStats;
import com.ty.hikingalone.interfaces.stats.converter.StatsConverter;
import com.ty.hikingalone.interfaces.stats.vo.query.AvailableYearVO;
import com.ty.hikingalone.interfaces.stats.vo.query.YearlyStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 年度统计应用服务：读时从徒步日记聚合，零表设计（个人量级无需预聚合）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    /** 年份参数下限 */
    private static final int MIN_YEAR = 2020;

    /** 年份参数上限 */
    private static final int MAX_YEAR = 2100;

    private final HikeJournalRepository journalRepository;
    private final StatsConverter converter;

    /**
     * 年度报告；year 缺省当前年，钳制 [2020, 2100]。某年无日记时返回全 0 结构
     */
    @Transactional(readOnly = true)
    public YearlyStatsVO annual(Long userId, Integer year) {
        int y = year == null ? LocalDate.now().getYear() : Math.max(MIN_YEAR, Math.min(MAX_YEAR, year));
        List<HikeJournal> journals = journalRepository.listByUserAndYear(userId, y);
        return converter.toVO(YearlyStats.assemble(y, journals));
    }

    /**
     * 可选年份：有日记的年份 ∪ 当前年，倒序，附徒步次数
     */
    @Transactional(readOnly = true)
    public List<AvailableYearVO> years(Long userId) {
        List<HikeJournal> journals = journalRepository.listByUser(userId);
        Set<Integer> years = new TreeSet<>((a, b) -> b - a);
        years.add(LocalDate.now().getYear());
        for (HikeJournal j : journals) {
            years.add(j.getHikeDate().getYear());
        }
        Set<Integer> ordered = new LinkedHashSet<>(years);
        return ordered.stream()
                .map(y -> converter.toYearVO(y, (int) journals.stream()
                        .filter(j -> j.getHikeDate().getYear() == y).count()))
                .toList();
    }
}
