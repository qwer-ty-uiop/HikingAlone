package com.ty.hikingalone.interfaces.gear.dto.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新建打包清单请求体
 */
@Data
public class GearChecklistSaveDTO {

    /**
     * 清单标题（1~100 字）
     */
    @NotBlank(message = "清单标题不能为空")
    private String title;

    /**
     * 对应出行日期（yyyy-MM-dd，可空）
     */
    private String hikeDate;

    /**
     * 备注（可空，≤200 字）
     */
    private String note;
}
