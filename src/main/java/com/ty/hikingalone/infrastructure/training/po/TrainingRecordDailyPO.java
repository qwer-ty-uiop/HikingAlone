package com.ty.hikingalone.infrastructure.training.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练项每日汇总表 PO
 */
@Data
@TableName("training_record_daily")
public class TrainingRecordDailyPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long itemId;

    private Long userId;

    private LocalDate recordDate;

    /**
     * 当日累计次数（times模式=Σcompleted_times；sets模式=Σ每组次数）
     */
    private Integer totalTimes;

    /**
     * 当日累计组数（sets模式=Σcompleted_sets；times模式=0）
     */
    private Integer totalSets;

    /**
     * 当日提交次数（热力图count）
     */
    private Integer commitCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
