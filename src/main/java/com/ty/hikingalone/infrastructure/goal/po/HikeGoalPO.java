package com.ty.hikingalone.infrastructure.goal.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 徒步目标表 PO
 */
@Data
@TableName("hike_goal")
public class HikeGoalPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 指标 1~6
     */
    private Integer metric;

    /**
     * 目标值
     */
    private BigDecimal targetValue;

    /**
     * 周期 1 月度 / 2 年度
     */
    private Integer periodType;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月 [1,12]；年度固定 0
     */
    private Integer month;

    /**
     * 0 已放弃 / 1 进行中
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
