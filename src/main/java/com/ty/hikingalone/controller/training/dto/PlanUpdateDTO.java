package com.ty.hikingalone.controller.training.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 编辑训练计划请求体
 */
@Data
public class PlanUpdateDTO {

    @NotNull(message = "计划id不能为空")
    private Long id;

    @NotBlank(message = "计划标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "周期开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "周期结束日期不能为空")
    private LocalDate endDate;

    /**
     * 周期类型：0不重复 1每天 2每周 3每月 4每年（缺省 0=不重复，老行为）
     */
    private Integer cycleType;

    /**
     * 周期锚点：每周=星期(1周一~7周日)；每月=日(1~31)；每年=月*100+日(如815=8月15日)；null=默认锚点
     */
    private Integer cycleAnchor;

    @Valid
    @NotEmpty(message = "至少需要一个训练项")
    private List<Item> items;

    /**
     * 训练项（id 非空=更新，空=新增；DB 有但未提交的项会被删除）
     */
    @Data
    public static class Item {

        private Long id;

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
