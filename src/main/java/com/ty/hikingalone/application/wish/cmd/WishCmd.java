package com.ty.hikingalone.application.wish.cmd;

import lombok.Data;

/**
 * 新建/编辑心愿命令（编辑时 id 必填；字段非空才覆盖）
 * <p>expectedDate 为 yyyy-MM-dd 文本，由应用服务解析</p>
 */
@Data
public class WishCmd {

    /** 心愿 id（编辑必填） */
    private Long id;

    /** 标题（1~100 字） */
    private String title;

    /** 地点描述（可空，≤200 字，null=不更新） */
    private String location;

    /** 期望出发日期（yyyy-MM-dd，可空） */
    private String expectedDate;

    /** 优先级（可空=不更新） */
    private Integer priority;

    /** 备注（可空，≤500 字，null=不更新） */
    private String note;

    /** 状态（可空=不更新；仅允许 0↔1） */
    private Integer status;
}
