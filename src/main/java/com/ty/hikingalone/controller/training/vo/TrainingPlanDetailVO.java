package com.ty.hikingalone.controller.training.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 训练计划详情视图对象：在列表项基础上增加周期内每日记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TrainingPlanDetailVO extends TrainingPlanVO {

    /**
     * 周期内每日提交记录
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
    }
}
