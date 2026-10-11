package com.ty.hikingalone.application.gear;

import com.ty.hikingalone.domain.gear.entity.GearItem;
import com.ty.hikingalone.domain.gear.repository.GearItemRepository;
import com.ty.hikingalone.domain.gear.service.GearAdvisor;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.interfaces.gear.vo.query.GearSuggestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * AI 装备建议应用服务：组装行程上下文（路线 + 现有装备）→ LLM 适配器 → 失败降级规则引擎。
 * <p>响应的 source 字段区分 LLM / RULE，前端透明展示；功能在未配置 LLM_API_KEY 时依然完整可用</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GearSuggestService {

    /** 行程天数上下限 */
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 7;

    /** 里程上限 */
    private static final BigDecimal MAX_DISTANCE_KM = new BigDecimal("500");

    /** 现有装备名参与去重的上限（防超长提示词；个人量级远达不到） */
    private static final int MAX_OWNED_ITEMS = 100;

    private final GearAdvisor gearAdvisor;
    private final GearItemRepository gearItemRepository;
    private final RouteTrackRepository routeTrackRepository;

    /**
     * 生成装备建议：routeId 有则带出线路上下文（越权即 400），LLM 不可用/失败时降级规则引擎
     */
    public GearSuggestVO suggest(Long userId, Long routeId, Integer days, BigDecimal distanceKm,
                                 Integer month, String extra) {
        // 1. 行程参数钳制
        int d = days == null ? 1 : Math.max(1, Math.min(7, days));
        int m = month == null ? LocalDate.now().getMonthValue() : Math.max(1, Math.min(12, month));
        String extraText = extra == null || extra.isBlank() ? null : extra.trim();

        // 2. 路线上下文（跨实体归属校验在应用服务）
        String routeName = null;
        BigDecimal effectiveDistance = distanceKm;
        BigDecimal elevationGain = null;
        Integer durationMin = null;
        if (routeId != null) {
            RouteTrack route = routeTrackRepository.findById(routeId);
            if (route == null || !userId.equals(route.getUserId())) {
                throw new IllegalArgumentException("关联的路线不存在");
            }
            routeName = route.getName();
            if (effectiveDistance == null) {
                effectiveDistance = route.getDistance();
            }
            elevationGain = route.getElevationGain();
            durationMin = route.getDurationMin();
        }
        if (effectiveDistance != null
                && (effectiveDistance.compareTo(BigDecimal.ZERO) <= 0
                || effectiveDistance.compareTo(MAX_DISTANCE_KM) > 0)) {
            throw new IllegalArgumentException("里程需在 0 ~ 500 km 之间");
        }

        // 3. 现有装备名（LLM 去重依据）
        List<String> ownedNames = gearItemRepository.listByUser(userId).stream()
                .limit(MAX_OWNED_ITEMS)
                .map(GearItem::getName)
                .toList();

        // 4. LLM 优先，失败/未配置降级规则引擎
        GearAdvisor.GearAdviceContext context = new GearAdvisor.GearAdviceContext(
                routeName, effectiveDistance, elevationGain, durationMin, d, m, extraText, ownedNames);
        Optional<GearAdvisor.GearAdvice> advice = gearAdvisor.advise(context);
        GearAdvisor.GearAdvice result = advice.orElseGet(() -> ruleAdvice(context));

        return GearSuggestVO.of(result);
    }

    /**
     * 内置规则引擎（LLM 未配置/失败时的兜底）：按品类与行程参数推导，文案通用
     */
    private GearAdvisor.GearAdvice ruleAdvice(GearAdvisor.GearAdviceContext ctx) {
        BigDecimal distance = ctx.distanceKm();
        boolean longDay = distance != null && distance.compareTo(new BigDecimal("15")) >= 0;
        boolean withWater = distance == null || distance.compareTo(new BigDecimal("8")) >= 0;
        boolean overnight = ctx.days() >= 2;

        List<GearAdvisor.GearAdviceItem> items = new java.util.ArrayList<>();
        items.add(new GearAdvisor.GearAdviceItem("背包（按行程选容积）", 1, null, 1, "所有装备的载体，一日 25L 起步，过夜 40L+"));
        items.add(new GearAdvisor.GearAdviceItem("徒步鞋袜", 4, null, 1, "合脚防滑是安全底线"));
        items.add(new GearAdvisor.GearAdviceItem("头灯及备用电池", 7, null, 1, "夜幕降临前必须能在手上"));
        items.add(new GearAdvisor.GearAdviceItem("手机 + 离线地图（两步路/奥维）", 8, null, 1, "山区信号不稳定，离线轨迹是保命符"));
        items.add(new GearAdvisor.GearAdviceItem("急救包（含止血敷料/水泡贴）", 9, null, 1, "水泡和擦伤是最常见的伤"));
        items.add(new GearAdvisor.GearAdviceItem("饮用水 2L", 6, null, 1, "按 500ml/小时补给节奏携带"));
        if (withWater) {
            items.add(new GearAdvisor.GearAdviceItem("能量补给（坚果/能量胶）", 6, null, 2, "长时间行走按 200kcal/小时补充"));
        }
        if (longDay) {
            items.add(new GearAdvisor.GearAdviceItem("登山杖", 11, null, 2, "下坡护膝、长距省力"));
            items.add(new GearAdvisor.GearAdviceItem("防晒（帽子+防晒霜）", 5, null, 2, "山里紫外线远强于城市"));
        }
        if (overnight) {
            items.add(new GearAdvisor.GearAdviceItem("帐篷/天幕", 2, null, 1, ctx.days() + " 天行程需要过夜庇护所"));
            items.add(new GearAdvisor.GearAdviceItem("睡袋（按季节温标）", 3, null, 1, "低温是失温主因，宁暖勿冷"));
            items.add(new GearAdvisor.GearAdviceItem("炉具与餐具", 6, null, 2, "热食是野外恢复体力的关键"));
            items.add(new GearAdvisor.GearAdviceItem("备用保暖衣物", 5, null, 2, "出汗后/到营后的干爽层防失温"));
        }
        items.add(new GearAdvisor.GearAdviceItem("充电宝与数据线", 10, null, 2, "手机导航是耗电大户"));
        items.add(new GearAdvisor.GearAdviceItem("雨衣/应急雨披", 5, null, 2, "山区天气多变，防雨保体温"));

        return new GearAdvisor.GearAdvice("RULE", "内置规则引擎", items, List.of(
                "最重的装备贴近背部中上部，行走重心最稳",
                "睡袋、备用衣物压包底，到营才取",
                "急救包和头灯放顶袋，伸手可及",
                "水袋/水壶放两侧网兜，行走中随时补水"));
    }
}
