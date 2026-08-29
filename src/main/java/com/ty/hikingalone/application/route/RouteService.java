package com.ty.hikingalone.application.route;

import com.ty.hikingalone.application.route.cmd.RouteCreateCmd;
import com.ty.hikingalone.domain.route.entity.RouteTrack;
import com.ty.hikingalone.domain.route.repository.RouteTrackRepository;
import com.ty.hikingalone.interfaces.route.converter.RouteConverter;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackDetailVO;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 路线轨迹模块应用服务：只做命令转换、流程编排与持久化触发，不含业务规则
 * <p>里程/时长/爬升的计算在聚合根 RouteTrack.create 内完成，应用层不重复实现</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteTrackRepository routeRepository;
    private final RouteConverter converter;

    /**
     * 创建路线：轨迹点与命名校验交给聚合根工厂，返回新路线 id
     */
    public Long createRoute(RouteCreateCmd cmd) {
        RouteTrack route = RouteTrack.create(cmd.userId(), cmd.name(), cmd.description(),
                cmd.points(), cmd.waypoints());
        return routeRepository.save(route);
    }

    /**
     * 我的路线列表（摘要，不含轨迹点）
     */
    public List<RouteTrackVO> listRoutes(Long userId) {
        return routeRepository.listByUserId(userId).stream()
                .map(converter::toRouteTrackVO)
                .toList();
    }

    /**
     * 路线详情（含完整轨迹点，供地图渲染）
     */
    public RouteTrackDetailVO getRouteDetail(Long userId, Long routeId) {
        RouteTrack route = requireOwnedRoute(userId, routeId);
        return converter.toRouteTrackDetailVO(route);
    }

    /**
     * 物理删除路线（先校验归属）
     */
    public void deleteRoute(Long userId, Long routeId) {
        requireOwnedRoute(userId, routeId);
        routeRepository.delete(routeId);
    }

    /**
     * 按 id 取路线并校验归属当前用户，防止跨用户越权访问/操作
     */
    private RouteTrack requireOwnedRoute(Long userId, Long routeId) {
        RouteTrack route = routeRepository.findById(routeId);
        if (route == null || !userId.equals(route.getUserId())) {
            throw new IllegalArgumentException("路线不存在");
        }
        return route;
    }
}
