package com.ty.hikingalone.infrastructure.training.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日训练记录持久化对象，与 training_record 表字段一一对应
 */
@Data
@TableName("training_record")
public class TrainingRecordPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Long itemId;

    private Long userId;

    private LocalDate recordDate;

    private Integer completedSets;

    private Integer completedTimes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
