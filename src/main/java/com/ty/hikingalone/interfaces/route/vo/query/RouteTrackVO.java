package com.ty.hikingalone.interfaces.route.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 路线列表项视图对象（摘要，不含轨迹点）
 */
@Data
public class RouteTrackVO {

    private Long id;

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
     * 轨迹点数
     */
    private Integer pointCount;

    /**
     * 创建时间（yyyy-MM-dd'T'HH:mm:ss）
     */
    private String createTime;
}
