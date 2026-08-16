package com.ty.hikingalone.controller.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建训练计划请求体
 */
@Data
public class PlanCreateDTO {

    @NotBlank(message = "计划标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "周期开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "周期结束日期不能为空")
    private LocalDate endDate;

    @NotEmpty(message = "至少需要一个训练项")
    private List<Item> items;

    /**
     * 训练项
     */
    @Data
    public static class Item {

        @NotBlank(message = "训练项名称不能为空")
        private String name;

        /**
         * 制定模式：times=按次数, sets=按次数+组数
         */
        @NotBlank(message = "制定模式不能为空")
        private String mode;

        /**
         * 目标次数（times模式=总次数；sets模式=每组次数）
         */
        @NotNull(message = "目标次数不能为空")
        private Integer totalTimes;

        /**
         * 目标组数（仅sets模式）
         */
        private Integer totalSets;

        /**
         * 单位，如：个/组/公里
         */
        private String unit;
    }
}
