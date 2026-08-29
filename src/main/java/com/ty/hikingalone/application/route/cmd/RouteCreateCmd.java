package com.ty.hikingalone.application.route.cmd;

import com.ty.hikingalone.domain.route.entity.RouteTrack;

import java.util.List;

/**
 * 创建路线命令：应用层入参，由接口层 Converter 从 HTTP DTO 组装
 *
 * @param waypoints 途径点/命名标注点（可为空集合）
 */
public record RouteCreateCmd(Long userId, String name, String description,
                             List<RouteTrack.Point> points, List<RouteTrack.Waypoint> waypoints) {
}
