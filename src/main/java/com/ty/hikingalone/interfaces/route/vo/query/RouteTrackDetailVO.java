package com.ty.hikingalone.interfaces.route.vo.query;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路线详情视图对象（含完整轨迹点，供地图渲染）
 */
@Data
public class RouteTrackDetailVO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal distance;

    private Integer durationMin;

    private BigDecimal elevationGain;

    private String createTime;

    private List<PointVO> points;

    /**
     * 途径点/命名标注点（无标注点时为空集合）
     */
    private List<WaypointVO> waypoints;

    /**
     * 轨迹点视图对象
     */
    @Data
    public static class PointVO {

        private Double lng;

        private Double lat;

        /**
         * 高程（米），可 null
         */
        private Double ele;

        /**
         * 时间点（yyyy-MM-dd'T'HH:mm:ss），可 null
         */
        private String time;
    }

    /**
     * 途径点（命名标注点）视图对象
     */
    @Data
    public static class WaypointVO {

        private Double lng;

        private Double lat;

        /**
         * 高程（米），可 null
         */
        private Double ele;

        /**
         * 标注名称，可 null
         */
        private String name;
    }
}
