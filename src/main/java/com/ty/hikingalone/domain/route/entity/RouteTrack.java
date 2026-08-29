package com.ty.hikingalone.domain.route.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 徒步路线轨迹领域实体（聚合根）
 * <p>保存用户上传的轨迹点，并在创建时由领域层统一计算 里程（Haversine 逐段累加）、时长（轨迹点时间戳首尾差）、
 * 累计爬升（相邻点高程正向差累加），保证统计口径在领域内收敛，应用层只做编排与持久化触发</p>
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RouteTrack {

    /**
     * 轨迹点值对象：经纬度 + 可选高程（米）+ 可选时间点（用于计算时长）
     */
    public record Point(double lng, double lat, Double ele, LocalDateTime time) {
    }

    /**
     * 途径点（命名标注点）值对象：经纬度 + 可选高程 + 名称，来自 GPX wpt / KML Point，用于在地图上标注途经地点
     */
    public record Waypoint(double lng, double lat, Double ele, String name) {
    }

    private Long id;

    private Long userId;

    private String name;

    private String description;

    /**
     * 总里程（公里）
     */
    private BigDecimal distance;

    /**
     * 总时长（分钟）
     */
    private Integer durationMin;

    /**
     * 累计爬升（米）
     */
    private BigDecimal elevationGain;

    /**
     * 轨迹点（按上传顺序，创建后不可变）
     */
    private List<Point> points = List.of();

    /**
     * 途径点/命名标注点（起点、终点、营地、补给点等，按文件顺序，创建后不可变；无标注点时为空）
     */
    private List<Waypoint> waypoints = List.of();

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：业务校验并创建一条路线，按轨迹点实时计算里程/时长/爬升；途径点可选
     */
    public static RouteTrack create(Long userId, String name, String description,
                                    List<Point> points, List<Waypoint> waypoints) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("路线名称不能为空");
        }
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("轨迹点至少需要2个");
        }
        for (Point point : points) {
            validatePoint(point);
        }
        List<Waypoint> safeWaypoints = waypoints == null ? List.of() : waypoints;
        for (Waypoint waypoint : safeWaypoints) {
            validateWaypoint(waypoint);
        }

        RouteTrack route = new RouteTrack();
        route.setUserId(userId);
        route.setName(name);
        route.setDescription(description);
        route.setPoints(List.copyOf(points));
        route.setWaypoints(List.copyOf(safeWaypoints));
        route.setDistance(round2(distanceKm(points)));
        route.setDurationMin(durationMin(points));
        route.setElevationGain(round1(elevationGain(points)));
        LocalDateTime now = LocalDateTime.now();
        route.setCreateTime(now);
        route.setUpdateTime(now);
        return route;
    }

    /**
     * 重建工厂：从持久化数据恢复路线，不执行业务校验与重算（指标已按创建时口径落库）
     */
    public static RouteTrack reconstruct(Long id, Long userId, String name, String description,
                                         BigDecimal distance, Integer durationMin, BigDecimal elevationGain,
                                         List<Point> points, List<Waypoint> waypoints,
                                         LocalDateTime createTime, LocalDateTime updateTime) {
        RouteTrack route = new RouteTrack();
        route.setId(id);
        route.setUserId(userId);
        route.setName(name);
        route.setDescription(description);
        route.setDistance(distance);
        route.setDurationMin(durationMin);
        route.setElevationGain(elevationGain);
        route.setPoints(points == null ? List.of() : List.copyOf(points));
        route.setWaypoints(waypoints == null ? List.of() : List.copyOf(waypoints));
        route.setCreateTime(createTime);
        route.setUpdateTime(updateTime);
        return route;
    }

    private static void validatePoint(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("轨迹点不能为空");
        }
        if (Double.isNaN(point.lng()) || Double.isNaN(point.lat())
                || Math.abs(point.lng()) > 180 || Math.abs(point.lat()) > 90) {
            throw new IllegalArgumentException("轨迹点经纬度非法");
        }
    }

    private static void validateWaypoint(Waypoint waypoint) {
        if (waypoint == null) {
            throw new IllegalArgumentException("途径点不能为空");
        }
        if (Double.isNaN(waypoint.lng()) || Double.isNaN(waypoint.lat())
                || Math.abs(waypoint.lng()) > 180 || Math.abs(waypoint.lat()) > 90) {
            throw new IllegalArgumentException("途径点经纬度非法");
        }
    }

    /**
     * 总里程（公里）：Haversine 球面距离，相邻点逐段累加
     */
    private static double distanceKm(List<Point> points) {
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            total += haversineKm(points.get(i - 1), points.get(i));
        }
        return total;
    }

    private static double haversineKm(Point a, Point b) {
        double r = 6371.0;
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLng = Math.toRadians(b.lng() - a.lng());
        double sinLat = Math.sin(dLat / 2);
        double sinLng = Math.sin(dLng / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat())) * sinLng * sinLng;
        return r * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    /**
     * 总时长（分钟）：首尾轨迹点时间戳差；任一端缺时间戳或未递增则按 0 处理
     */
    private static Integer durationMin(List<Point> points) {
        LocalDateTime first = points.get(0).time();
        LocalDateTime last = points.get(points.size() - 1).time();
        if (first == null || last == null || !last.isAfter(first)) {
            return 0;
        }
        long seconds = Duration.between(first, last).getSeconds();
        return (int) Math.round(seconds / 60.0);
    }

    /**
     * 累计爬升（米）：相邻点高程正向差累加；任一端缺高程（null/NaN）则跳过该段
     */
    private static double elevationGain(List<Point> points) {
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            Double prev = points.get(i - 1).ele();
            Double cur = points.get(i).ele();
            if (prev == null || cur == null || Double.isNaN(prev) || Double.isNaN(cur)) {
                continue;
            }
            double delta = cur - prev;
            if (delta > 0) {
                total += delta;
            }
        }
        return total;
    }

    private static BigDecimal round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }
}
