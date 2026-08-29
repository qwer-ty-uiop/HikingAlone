package com.ty.hikingalone.infrastructure.route.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.infrastructure.route.mapper.RouteTrackMapper;
import com.ty.hikingalone.infrastructure.route.po.RouteTrackPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 路线轨迹仓储实现：领域接口的数据库实现
 * <p>轨迹点以 JSON 列随路线同表持久化，序列化/反序列化由本层负责，领域实体保持类型安全</p>
 */
@Repository
@RequiredArgsConstructor
public class RouteTrackRepositoryImpl implements RouteTrackRepository {

    private final RouteTrackMapper routeTrackMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Long save(RouteTrack route) {
        RouteTrackPO po = new RouteTrackPO();
        po.setUserId(route.getUserId());
        po.setName(route.getName());
        po.setDescription(route.getDescription());
        po.setDistance(route.getDistance());
        po.setDurationMin(route.getDurationMin());
        po.setElevationGain(route.getElevationGain());
        po.setPointsJson(writePoints(route.getPoints()));
        po.setWaypointsJson(writeWaypoints(route.getWaypoints()));
        po.setCreateTime(route.getCreateTime());
        po.setUpdateTime(route.getUpdateTime());
        routeTrackMapper.insert(po);
        route.setId(po.getId());
        return po.getId();
    }

    @Override
    public RouteTrack findById(Long id) {
        RouteTrackPO po = routeTrackMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public List<RouteTrack> listByUserId(Long userId) {
        return routeTrackMapper.selectList(
                new LambdaQueryWrapper<RouteTrackPO>()
                        .eq(RouteTrackPO::getUserId, userId)
                        .orderByDesc(RouteTrackPO::getCreateTime)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public void delete(Long id) {
        routeTrackMapper.deleteById(id);
    }

    private RouteTrack toEntity(RouteTrackPO po) {
        return RouteTrack.reconstruct(
                po.getId(), po.getUserId(), po.getName(), po.getDescription(),
                po.getDistance(), po.getDurationMin(), po.getElevationGain(),
                readPoints(po.getPointsJson()), readWaypoints(po.getWaypointsJson()),
                po.getCreateTime(), po.getUpdateTime());
    }

    private String writePoints(List<RouteTrack.Point> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (Exception e) {
            throw new IllegalStateException("轨迹点序列化失败", e);
        }
    }

    private List<RouteTrack.Point> readPoints(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RouteTrack.Point>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("轨迹点反序列化失败", e);
        }
    }

    private String writeWaypoints(List<RouteTrack.Waypoint> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(waypoints);
        } catch (Exception e) {
            throw new IllegalStateException("途径点序列化失败", e);
        }
    }

    private List<RouteTrack.Waypoint> readWaypoints(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RouteTrack.Waypoint>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("途径点反序列化失败", e);
        }
    }
}
