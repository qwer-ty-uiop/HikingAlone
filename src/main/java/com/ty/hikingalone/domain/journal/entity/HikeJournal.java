package com.ty.hikingalone.domain.journal.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徒步日记领域实体（聚合根）
 * <p>一次徒步一条完整记述，事后修改语义为「补充/修正记述」而非新事件，按聚合主键覆盖更新。
 * 里程/时长/爬升/难度/心情等不变量内聚在工厂与 update 方法中；
 * route_id 的归属校验与轨迹指标带出属跨实体规则，由应用服务完成</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HikeJournal {

    /** 难度下限（1 休闲） */
    public static final int MIN_DIFFICULTY = 1;

    /** 难度上限（5 地狱） */
    public static final int MAX_DIFFICULTY = 5;

    /** 心情下限（1 疲惫） */
    public static final int MIN_MOOD = 1;

    /** 心情上限（5 震撼） */
    public static final int MAX_MOOD = 5;

    /** 标题长度上限 */
    public static final int MAX_TITLE_LENGTH = 100;

    /** 天气描述长度上限 */
    public static final int MAX_WEATHER_LENGTH = 50;

    /** 同伴描述长度上限 */
    public static final int MAX_COMPANIONS_LENGTH = 100;

    /** 正文长度上限 */
    public static final int MAX_CONTENT_LENGTH = 8000;

    /** 里程上限 km（单次徒步合理上限） */
    public static final BigDecimal MAX_DISTANCE_KM = new BigDecimal("500.00");

    /** 时长上限 分钟（3 天） */
    public static final int MAX_DURATION_MIN = 4320;

    /** 累计爬升上限 米 */
    public static final BigDecimal MAX_ELEVATION_GAIN = new BigDecimal("10000.0");

    /** 花费上限 */
    public static final BigDecimal MAX_COST = new BigDecimal("999999.99");

    private Long id;

    private Long userId;

    private String title;

    private LocalDate hikeDate;

    /**
     * 关联轨迹 id（可空；归属校验在应用服务）
     */
    private Long routeId;

    /**
     * 里程 km（可空，2 位小数）
     */
    private BigDecimal distanceKm;

    /**
     * 时长 分钟（可空）
     */
    private Integer durationMin;

    /**
     * 累计爬升 m（可空，1 位小数）
     */
    private BigDecimal elevationGain;

    /**
     * 难度 1休闲 2轻松 3进阶 4挑战 5地狱
     */
    private Integer difficulty;

    /**
     * 心情 1疲惫 2平静 3愉悦 4兴奋 5震撼
     */
    private Integer mood;

    /**
     * 天气描述（可空，trim 后落库）
     */
    private String weather;

    /**
     * 同伴（可空，「独行」或同伴名，trim 后落库）
     */
    private String companions;

    /**
     * 本次总花费（可空，2 位小数）
     */
    private BigDecimal cost;

    /**
     * 正文（可空，trim 后落库）
     */
    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：新建日记，校验全部不变量
     */
    public static HikeJournal record(Long userId, String title, LocalDate hikeDate, Long routeId,
                                     BigDecimal distanceKm, Integer durationMin, BigDecimal elevationGain,
                                     Integer difficulty, Integer mood, String weather, String companions,
                                     BigDecimal cost, String content) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        HikeJournal journal = new HikeJournal();
        journal.userId = userId;
        journal.createTime = LocalDateTime.now();
        journal.applyContent(title, hikeDate, routeId, distanceKm, durationMin, elevationGain,
                difficulty, mood, weather, companions, cost, content);
        return journal;
    }

    /**
     * 编辑日记：全量替换记述内容（不变量同新建），刷新 updateTime。
     * <p>不改 userId/createTime</p>
     */
    public void update(String title, LocalDate hikeDate, Long routeId,
                       BigDecimal distanceKm, Integer durationMin, BigDecimal elevationGain,
                       Integer difficulty, Integer mood, String weather, String companions,
                       BigDecimal cost, String content) {
        applyContent(title, hikeDate, routeId, distanceKm, durationMin, elevationGain,
                difficulty, mood, weather, companions, cost, content);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 记述内容组装与校验：标题/日期/难度/心情必填且合法，可选数值落在合理区间，文本限长
     */
    private void applyContent(String title, LocalDate hikeDate, Long routeId,
                              BigDecimal distanceKm, Integer durationMin, BigDecimal elevationGain,
                              Integer difficulty, Integer mood, String weather, String companions,
                              BigDecimal cost, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("标题不能超过 100 字");
        }
        if (hikeDate == null) {
            throw new IllegalArgumentException("徒步日期不能为空");
        }
        if (hikeDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("徒步日期不能是未来");
        }
        if (difficulty == null || difficulty < MIN_DIFFICULTY || difficulty > MAX_DIFFICULTY) {
            throw new IllegalArgumentException("难度需在 1 ~ 5 之间");
        }
        if (mood == null || mood < MIN_MOOD || mood > MAX_MOOD) {
            throw new IllegalArgumentException("心情需在 1 ~ 5 之间");
        }
        if (distanceKm != null) {
            BigDecimal d = distanceKm.setScale(2, RoundingMode.HALF_UP);
            if (d.compareTo(BigDecimal.ZERO) <= 0 || d.compareTo(MAX_DISTANCE_KM) > 0) {
                throw new IllegalArgumentException("里程需在 0 ~ 500 km 之间");
            }
            distanceKm = d;
        }
        if (durationMin != null && (durationMin < 1 || durationMin > MAX_DURATION_MIN)) {
            throw new IllegalArgumentException("时长需在 1 ~ 4320 分钟之间");
        }
        if (elevationGain != null) {
            BigDecimal e = elevationGain.setScale(1, RoundingMode.HALF_UP);
            if (e.compareTo(BigDecimal.ZERO) < 0 || e.compareTo(MAX_ELEVATION_GAIN) > 0) {
                throw new IllegalArgumentException("累计爬升需在 0 ~ 10000 米之间");
            }
            elevationGain = e;
        }
        if (weather != null && weather.trim().length() > MAX_WEATHER_LENGTH) {
            throw new IllegalArgumentException("天气描述不能超过 50 字");
        }
        if (companions != null && companions.trim().length() > MAX_COMPANIONS_LENGTH) {
            throw new IllegalArgumentException("同伴描述不能超过 100 字");
        }
        if (cost != null) {
            BigDecimal c = cost.setScale(2, RoundingMode.HALF_UP);
            if (c.compareTo(BigDecimal.ZERO) < 0 || c.compareTo(MAX_COST) > 0) {
                throw new IllegalArgumentException("花费需在 0 ~ 999999.99 元之间");
            }
            cost = c;
        }
        if (content != null && content.trim().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("正文不能超过 8000 字");
        }
        this.title = t;
        this.hikeDate = hikeDate;
        this.routeId = routeId;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.elevationGain = elevationGain;
        this.difficulty = difficulty;
        this.mood = mood;
        this.weather = weather == null || weather.isBlank() ? null : weather.trim();
        this.companions = companions == null || companions.isBlank() ? null : companions.trim();
        this.cost = cost;
        this.content = content == null || content.isBlank() ? null : content.trim();
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static HikeJournal reconstruct(Long id, Long userId, String title, LocalDate hikeDate, Long routeId,
                                          BigDecimal distanceKm, Integer durationMin, BigDecimal elevationGain,
                                          Integer difficulty, Integer mood, String weather, String companions,
                                          BigDecimal cost, String content,
                                          LocalDateTime createTime, LocalDateTime updateTime) {
        HikeJournal journal = new HikeJournal();
        journal.id = id;
        journal.userId = userId;
        journal.title = title;
        journal.hikeDate = hikeDate;
        journal.routeId = routeId;
        journal.distanceKm = distanceKm;
        journal.durationMin = durationMin;
        journal.elevationGain = elevationGain;
        journal.difficulty = difficulty;
        journal.mood = mood;
        journal.weather = weather;
        journal.companions = companions;
        journal.cost = cost;
        journal.content = content;
        journal.createTime = createTime;
        journal.updateTime = updateTime;
        return journal;
    }
}
