package com.ty.hikingalone.controller.training.vo;

import lombok.Data;

import java.util.List;

/**
 * 训练热力图视图对象（日历热力图）
 */
@Data
public class TrainingHeatmapVO {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 每日提交记录数
     */
    private List<DayVO> days;

    /**
     * 单日视图对象
     */
    @Data
    public static class DayVO {

        /**
         * 日期 yyyy-MM-dd
         */
        private String date;

        /**
         * 当天提交的记录条数（未提交的天不返回）
         */
        private Integer count;
    }
}
