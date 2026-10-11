package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI 装备建议请求体（POST /gear/suggest）：全字段可选，routeId 优先带出线路上下文
 */
@Data
public class GearSuggestDTO {

    /**
     * 关联路线 id（可空；须属于当前用户，自动带出里程/爬升/时长）
     */
    private Long routeId;

    /**
     * 行程天数（可空，缺省 1，钳制 1~7）
     */
    @Min(value = 1, message = "行程天数至少 1 天")
    @Max(value = 7, message = "行程天数最多 7 天")
    private Integer days;

    /**
     * 显式里程 km（可空；无 routeId 时建议提供，(0,500]）
     */
    @DecimalMin(value = "0.01", message = "里程需大于 0")
    @DecimalMax(value = "500", message = "里程需在 0 ~ 500 km 之间")
    private BigDecimal distanceKm;

    /**
     * 出行月份（可空，缺省当前月，1~12）
     */
    @Min(value = 1, message = "月份需在 1 ~ 12 之间")
    @Max(value = 12, message = "月份需在 1 ~ 12 之间")
    private Integer month;

    /**
     * 自由补充（可空，≤200 字：天气/同伴/宠物等）
     */
    @Size(max = 200, message = "补充说明不能超过 200 字")
    private String extra;
}
