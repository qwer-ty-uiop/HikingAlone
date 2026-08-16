package com.ty.hikingalone.controller.training.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练计划列表项视图对象
 */
@Data
public class TrainingPlanVO {

    private Long id;

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 状态：0已放弃 1进行中 2已完成 3已过期
     */
    private Integer status;

    /**
     * 总进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 训练项
     */
    private List<ItemVO> items;

    /**
     * 训练项视图对象
     */
    @Data
    public static class ItemVO {

        private Long id;

        private String name;

        private String mode;

        private Integer totalTimes;

        private Integer totalSets;

        private String unit;

        /**
         * 累计已完成值（times模式=累计次数；sets模式=累计组数）
         */
        private Integer doneValue;

        /**
         * 该项是否已达标
         */
        private Boolean done;
    }
}
