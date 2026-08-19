package com.ty.hikingalone.interfaces.training.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑单条打卡记录请求体（仅完成量）
 */
@Data
public class RecordUpdateDTO {

    @NotNull(message = "记录id不能为空")
    private Long id;

    /**
     * 修改后的完成组数（sets模式；times模式为0）
     */
    private Integer completedSets;

    /**
     * 修改后的完成次数（times模式=总次数；sets模式=每组次数，不传则用计划默认值）
     */
    private Integer completedTimes;
}
