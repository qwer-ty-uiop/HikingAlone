package com.ty.hikingalone.infrastructure.wish.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徒步心愿表 PO
 */
@Data
@TableName("hike_wish")
public class HikeWishPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /**
     * 地点描述（可空）
     */
    private String location;

    /**
     * 期望出发日期（可空）
     */
    private LocalDate expectedDate;

    /**
     * 优先级 1~3
     */
    private Integer priority;

    /**
     * 备注（可空）
     */
    private String note;

    /**
     * 0 想去 / 1 已计划 / 2 已完成 / 3 已放弃
     */
    private Integer status;

    /**
     * 完成日期（可空）
     */
    private LocalDate doneDate;

    /**
     * 完成后关联的日记 id（可空）
     */
    private Long journalId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
