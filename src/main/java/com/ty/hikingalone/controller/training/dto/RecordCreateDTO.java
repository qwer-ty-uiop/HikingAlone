package com.ty.hikingalone.controller.training.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交单个训练项当天完成情况请求体
 */
@Data
public class RecordCreateDTO {

    @NotNull(message = "计划id不能为空")
    private Long planId;

    @NotNull(message = "训练项id不能为空")
    private Long itemId;

    @NotNull(message = "记录日期不能为空")
    private String recordDate;

    /**
     * 当天完成组数（sets模式必填；times模式为0）
     */
    private Integer completedSets;

    /**
     * 当天完成次数（times模式=当天总次数；sets模式=每组次数，不传则用计划默认值）
     */
    private Integer completedTimes;
}
