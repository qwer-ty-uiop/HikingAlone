package com.ty.hikingalone.common.enums.training;

import lombok.Getter;

/**
 * 训练项制定模式枚举
 */
@Getter
public enum TrainingModeEnum {

    TIMES("times", "按次数"),
    SETS("sets", "按次数+组数");

    private final String code;
    private final String desc;

    TrainingModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
