package com.ty.hikingalone.interfaces.wish.vo.query;

import lombok.Data;

/**
 * 徒步心愿 VO
 */
@Data
public class WishVO {

    private Long id;

    /** 标题 */
    private String title;

    /** 地点描述（可空） */
    private String location;

    /** 期望出发日期 yyyy-MM-dd（可空） */
    private String expectedDate;

    /** 优先级 1~3 */
    private Integer priority;

    /** 备注（可空） */
    private String note;

    /** 0 想去 / 1 已计划 / 2 已完成 / 3 已放弃 */
    private Integer status;

    /** 完成日期 yyyy-MM-dd（可空） */
    private String doneDate;

    /** 关联日记 id（可空） */
    private Long journalId;

    /** 关联日记标题（未关联/日记被删为 null） */
    private String journalTitle;

    /** 创建时间（yyyy-MM-dd'T'HH:mm:ss） */
    private String createTime;

    /** 最近修改时间（未修改过为 null） */
    private String updateTime;
}
