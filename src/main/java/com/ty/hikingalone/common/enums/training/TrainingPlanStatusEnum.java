package com.ty.hikingalone.common.enums.training;

import lombok.Getter;

/**
 * 训练计划状态枚举
 */
@Getter
public enum TrainingPlanStatusEnum {

    ABANDONED(0, "已放弃"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    EXPIRED(3, "已过期");

    private final Integer code;
    private final String desc;

    TrainingPlanStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
