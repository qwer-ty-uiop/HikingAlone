package com.ty.hikingalone.infrastructure.training.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 训练计划项持久化对象，与 training_plan_item 表字段一一对应
 */
@Data
@TableName("training_plan_item")
public class TrainingPlanItemPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private String name;

    private String mode;

    private Integer totalTimes;

    private Integer totalSets;

    private String unit;

    private Integer sort;
}
