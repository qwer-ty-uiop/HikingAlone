package com.ty.hikingalone.interfaces.route.converter;

import com.ty.hikingalone.application.route.cmd.RouteCreateCmd;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.interfaces.route.dto.command.RouteCreateDTO;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackDetailVO;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 路线模块接口层转换器：HTTP DTO → 应用层命令；领域实体 → 视图对象
 */
@Component
public class RouteConverter {

    public RouteCreateCmd toRouteCreateCmd(Long userId, RouteCreateDTO dto) {
        List<RouteTrack.Point> points = dto.getPoints().stream()
                .map(this::toPoint)
                .toList();
        List<RouteTrack.Waypoint> waypoints = dto.getWaypoints() == null
                ? List.of()
                : dto.getWaypoints().stream().map(this::toWaypoint).toList();
        return new RouteCreateCmd(userId, dto.getName(), dto.getDescription(), points, waypoints);
    }

    private RouteTrack.Point toPoint(RouteCreateDTO.Point point) {
        LocalDateTime time = null;
        if (point.getTime() != null && !point.getTime().isBlank()) {
            try {
                time = LocalDateTime.parse(point.getTime());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("轨迹点时间格式错误，应为 yyyy-MM-dd'T'HH:mm:ss");
            }
        }
        return new RouteTrack.Point(point.getLng(), point.getLat(), point.getEle(), time);
    }

    private RouteTrack.Waypoint toWaypoint(RouteCreateDTO.Waypoint waypoint) {
        return new RouteTrack.Waypoint(waypoint.getLng(), waypoint.getLat(),
                waypoint.getEle(), waypoint.getName());
    }

    public RouteTrackVO toRouteTrackVO(RouteTrack route) {
        RouteTrackVO vo = new RouteTrackVO();
        vo.setId(route.getId());
        vo.setName(route.getName());
        vo.setDescription(route.getDescription());
        vo.setDistance(route.getDistance());
        vo.setDurationMin(route.getDurationMin());
        vo.setElevationGain(route.getElevationGain());
        vo.setPointCount(route.getPoints().size());
        vo.setCreateTime(route.getCreateTime() == null ? null : route.getCreateTime().withNano(0).toString());
        return vo;
    }

    public RouteTrackDetailVO toRouteTrackDetailVO(RouteTrack route) {
        RouteTrackDetailVO vo = new RouteTrackDetailVO();
        vo.setId(route.getId());
        vo.setName(route.getName());
        vo.setDescription(route.getDescription());
        vo.setDistance(route.getDistance());
        vo.setDurationMin(route.getDurationMin());
        vo.setElevationGain(route.getElevationGain());
        vo.setCreateTime(route.getCreateTime() == null ? null : route.getCreateTime().withNano(0).toString());
        vo.setPoints(route.getPoints().stream().map(this::toPointVO).toList());
        vo.setWaypoints(route.getWaypoints().stream().map(this::toWaypointVO).toList());
        return vo;
    }

    private RouteTrackDetailVO.PointVO toPointVO(RouteTrack.Point point) {
        RouteTrackDetailVO.PointVO pointVO = new RouteTrackDetailVO.PointVO();
        pointVO.setLng(point.lng());
        pointVO.setLat(point.lat());
        pointVO.setEle(point.ele());
        pointVO.setTime(point.time() == null ? null : point.time().withNano(0).toString());
        return pointVO;
    }

    private RouteTrackDetailVO.WaypointVO toWaypointVO(RouteTrack.Waypoint waypoint) {
        RouteTrackDetailVO.WaypointVO waypointVO = new RouteTrackDetailVO.WaypointVO();
        waypointVO.setLng(waypoint.lng());
        waypointVO.setLat(waypoint.lat());
        waypointVO.setEle(waypoint.ele());
        waypointVO.setName(waypoint.name());
        return waypointVO;
    }
}
