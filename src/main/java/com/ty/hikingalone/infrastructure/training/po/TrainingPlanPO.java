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

    /**
     * 周期类型：0不重复 1每天 2每周 3每月 4每年
     */
    private Integer cycleType;

    /**
     * 周期锚点：每周=星期(1周一~7周日)；每月=日(1~31)；每年=月*100+日；null=默认锚点
     */
    private Integer cycleAnchor;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
