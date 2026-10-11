package com.ty.hikingalone.application.achievement;

import com.ty.hikingalone.domain.achievement.AchievementCatalog;
import com.ty.hikingalone.domain.achievement.entity.Achievement;
import com.ty.hikingalone.domain.achievement.entity.AchievementDefinition;
import com.ty.hikingalone.domain.achievement.entity.AchievementMetric;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.training.repository.TrainingRecordDailyRepository;
import com.ty.hikingalone.interfaces.achievement.converter.AchievementConverter;
import com.ty.hikingalone.interfaces.achievement.vo.query.AchievementGroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成就徽章应用服务：跨域只读聚合（徒步日记 + 训练打卡），套目录实时计算达成状态。
 * <p>零表设计：徽章不落库，数据被删徽章自然回落，避免发出去的徽章与数据不一致</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final HikeJournalRepository journalRepository;
    private final TrainingRecordDailyRepository trainingRecordDailyRepository;
    private final AchievementConverter converter;

    /**
     * 全部徽章（按分组）
     */
    @Transactional(readOnly = true)
    public List<AchievementGroupVO> all(Long userId) {
        Map<AchievementMetric, BigDecimal> metrics = collectMetrics(userId);
        // 目录顺序即展示顺序；分组用 LinkedHashMap 保持首次出现次序
        Map<String, List<Achievement>> grouped = new LinkedHashMap<>();
        for (AchievementDefinition def : AchievementCatalog.all()) {
            grouped.computeIfAbsent(def.group(), k -> new ArrayList<>())
                    .add(Achievement.of(def, metrics.get(def.metric())));
        }
        return converter.toGroupVOList(grouped);
    }

    /**
     * 各口径实时值：日记聚合（次数/累计里程/累计爬升/单日最远）+ 训练打卡总次数
     */
    private Map<AchievementMetric, BigDecimal> collectMetrics(Long userId) {
        List<HikeJournal> journals = journalRepository.listByUser(userId);
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal totalElevation = BigDecimal.ZERO;
        BigDecimal maxSingleDistance = BigDecimal.ZERO;
        for (HikeJournal j : journals) {
            if (j.getDistanceKm() != null) {
                totalDistance = totalDistance.add(j.getDistanceKm());
                if (j.getDistanceKm().compareTo(maxSingleDistance) > 0) {
                    maxSingleDistance = j.getDistanceKm();
                }
            }
            if (j.getElevationGain() != null) {
                totalElevation = totalElevation.add(j.getElevationGain());
            }
        }
        Map<AchievementMetric, BigDecimal> metrics = new EnumMap<>(AchievementMetric.class);
        metrics.put(AchievementMetric.HIKE_COUNT, BigDecimal.valueOf(journals.size()));
        metrics.put(AchievementMetric.TOTAL_DISTANCE_KM, totalDistance);
        metrics.put(AchievementMetric.TOTAL_ELEVATION_M, totalElevation);
        metrics.put(AchievementMetric.MAX_SINGLE_DISTANCE_KM, maxSingleDistance);
        metrics.put(AchievementMetric.TRAIN_CHECKIN_COUNT,
                BigDecimal.valueOf(trainingRecordDailyRepository.sumCommitCount(userId)));
        return metrics;
    }
}
