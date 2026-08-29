package com.ty.hikingalone.interfaces.route;

import com.ty.hikingalone.application.route.RouteService;
import com.ty.hikingalone.application.route.cmd.RouteCreateCmd;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.route.converter.RouteConverter;
import com.ty.hikingalone.interfaces.route.dto.command.RouteCreateDTO;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackDetailVO;
import com.ty.hikingalone.interfaces.route.vo.query.RouteTrackVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 路线轨迹模块控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteConverter routeConverter;

    /**
     * 上传/创建路线轨迹：传入轨迹点，里程/时长/爬升由领域层计算
     */
    @PostMapping
    public Result<Long> createRoute(@Valid @RequestBody RouteCreateDTO dto) {
        RouteCreateCmd cmd = routeConverter.toRouteCreateCmd(LoginUserContext.getUserId(), dto);
        return Result.success(routeService.createRoute(cmd));
    }

    /**
     * 我的路线列表（摘要，不含轨迹点）
     */
    @GetMapping
    public Result<List<RouteTrackVO>> listRoutes() {
        return Result.success(routeService.listRoutes(LoginUserContext.getUserId()));
    }

    /**
     * 路线详情（含完整轨迹点，供地图渲染）
     */
    @GetMapping("/{id}")
    public Result<RouteTrackDetailVO> getRouteDetail(@PathVariable Long id) {
        return Result.success(routeService.getRouteDetail(LoginUserContext.getUserId(), id));
    }

    /**
     * 物理删除路线
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(LoginUserContext.getUserId(), id);
        return Result.success();
    }
}
