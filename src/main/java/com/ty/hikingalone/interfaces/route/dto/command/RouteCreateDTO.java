package com.ty.hikingalone.interfaces.route.dto.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建路线请求体
 * <p>轨迹点由前端从 GPX 等文件解析后提交，里程/时长/爬升由后端领域层计算，前端无需上传计算结果</p>
 */
@Data
public class RouteCreateDTO {

    @NotBlank(message = "路线名称不能为空")
    private String name;

    private String description;

    @NotEmpty(message = "轨迹点不能为空")
    @Valid
    private List<Point> points;

    /**
     * 途径点/命名标注点（可选，来自 GPX wpt / KML Point Placemark）；无标注点时可缺省
     */
    @Valid
    private List<Waypoint> waypoints;

    /**
     * 轨迹点：经纬度必填；高程/时间可选（时间用于计算时长）
     */
    @Data
    public static class Point {

        @NotNull(message = "经度不能为空")
        private Double lng;

        @NotNull(message = "纬度不能为空")
        private Double lat;

        /**
         * 高程（米），可选
         */
        private Double ele;

        /**
         * 时间点（ISO 格式 yyyy-MM-dd'T'HH:mm:ss），可选
         */
        private String time;
    }

    /**
     * 途径点（命名标注点）：经纬度必填，高程/名称可选
     */
    @Data
    public static class Waypoint {

        @NotNull(message = "途径点经度不能为空")
        private Double lng;

        @NotNull(message = "途径点纬度不能为空")
        private Double lat;

        /**
         * 高程（米），可选
         */
        private Double ele;

        /**
         * 标注名称（如：起点、补给点、山头名）
         */
        private String name;
    }
}
