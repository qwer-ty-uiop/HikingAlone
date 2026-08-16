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
     * 每日提交记录（按日期升序；未提交的天不返回）
     */
    private List<RecordVO> records;

    /**
     * 单日单训练项记录视图对象
     */
    @Data
    public static class RecordVO {

        private String date;

        private Long itemId;

        private Integer completedSets;

        private Integer completedTimes;

        /**
         * 本次提交时间（yyyy-MM-dd'T'HH:mm:ss）；append 模型下每条记录一次提交，同日可多条
         */
        private String createTime;
    }

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
         * 剩余任务量（目标值-已完成值，已达标为0；times模式=剩余次数；sets模式=剩余组数）
         */
        private Integer remainValue;

        /**
         * 该项是否已达标
         */
        private Boolean done;
    }
}
