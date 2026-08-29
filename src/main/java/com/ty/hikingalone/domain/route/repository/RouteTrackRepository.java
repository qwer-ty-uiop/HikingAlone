package com.ty.hikingalone.domain.route.repository;

import com.ty.hikingalone.domain.route.entity.RouteTrack;

import java.util.List;

/**
 * 路线轨迹仓储接口
 * <p>以 RouteTrack 为聚合根；轨迹点随路线同表持久化（JSON 列），由仓储负责序列化转换</p>
 */
public interface RouteTrackRepository {

    /**
     * 保存路线，返回路线id（并回填实体 id）
     */
    Long save(RouteTrack route);

    /**
     * 按id查询路线（含轨迹点）
     */
    RouteTrack findById(Long id);

    /**
     * 按用户查询路线列表，按创建时间倒序
     */
    List<RouteTrack> listByUserId(Long userId);

    /**
     * 物理删除路线
     */
    void delete(Long id);
}
