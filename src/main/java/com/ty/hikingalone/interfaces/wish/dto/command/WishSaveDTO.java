package com.ty.hikingalone.interfaces.wish.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新建心愿请求体
 */
@Data
public class WishSaveDTO {

    /**
     * 标题（1~100 字）
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 地点描述（可空，≤200 字）
     */
    private String location;

    /**
     * 期望出发日期（yyyy-MM-dd，可空）
     */
    private String expectedDate;

    /**
     * 优先级 1~3（可空，默认 2）
     */
    private Integer priority;

    /**
     * 备注（可空，≤500 字）
     */
    private String note;
}
