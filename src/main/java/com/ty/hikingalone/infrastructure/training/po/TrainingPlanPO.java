package com.ty.hikingalone.infrastructure.training.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练计划持久化对象，与 training_plan 表字段一一对应
 */
@Data
@TableName("training_plan")
public class TrainingPlanPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
