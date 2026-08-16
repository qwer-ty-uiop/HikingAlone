package com.ty.hikingalone.controller.training.vo;

import lombok.Data;

import java.util.List;

/**
 * 训练首页聚合视图对象，对应 GET /train
 */
@Data
public class TrainingHomePageVO {

    /**
     * 近期训练计划（含进度）
     */
    private List<TrainingPlanVO> plans;

    /**
     * 今年训练热力图
     */
    private TrainingHeatmapVO heatmap;
}
