package com.ty.hikingalone.infrastructure.route.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 路线轨迹持久化对象，与 route_track 表字段一一对应
 */
@Data
@TableName("route_track")
public class RouteTrackPO {

    @TableId(type = IdType.AUTO)
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
     * 轨迹点 JSON：[{"lng":..,"lat":..,"ele":..,"time":".."}]
     */
    private String pointsJson;

    /**
     * 途径点/命名标注点 JSON：[{"lng":..,"lat":..,"ele":..,"name":".."}]，无标注点时为 null
     */
    private String waypointsJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
