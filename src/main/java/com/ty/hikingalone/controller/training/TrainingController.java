package com.ty.hikingalone.controller.training;

import com.ty.hikingalone.application.train.TrainingService;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.controller.training.dto.PlanCreateDTO;
import com.ty.hikingalone.controller.training.dto.RecordCreateDTO;
import com.ty.hikingalone.controller.training.vo.TrainingHeatmapVO;
import com.ty.hikingalone.controller.training.vo.TrainingHomePageVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanDetailVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训练模块控制器：接口层，只做 HTTP 适配
 * <p>暂无登录体系，统一使用固定用户 user_id=1</p>
 */
@Slf4j
@RestController
@RequestMapping("/train")
@RequiredArgsConstructor
public class TrainingController {

    /**
     * 暂无登录体系，先固定用户
     */
    private static final Long USER_ID = 1L;

    private final TrainingService trainingService;

    /**
     * 训练首页聚合：近期计划 + 今年热力图
     */
    @GetMapping
    public Result<TrainingHomePageVO> homePage() {
        return Result.success(trainingService.getHomePage(USER_ID));
    }

    /**
     * 制定训练计划
     */
    @PostMapping("/plans")
    public Result<Long> createPlan(@Valid @RequestBody PlanCreateDTO dto) {
        return Result.success(trainingService.createPlan(USER_ID, dto));
    }

    /**
     * 训练计划列表（含进度）
     */
    @GetMapping("/plans")
    public Result<List<TrainingPlanVO>> listPlans() {
        return Result.success(trainingService.listPlans(USER_ID));
    }

    /**
     * 训练计划详情（含每日提交记录）
     */
    @GetMapping("/plans/{id}")
    public Result<TrainingPlanDetailVO> getPlanDetail(@PathVariable Long id) {
        return Result.success(trainingService.getPlanDetail(id));
    }

    /**
     * 提交单个训练项当天完成情况
     */
    @PostMapping("/records")
    public Result<Void> submitRecord(@Valid @RequestBody RecordCreateDTO dto) {
        trainingService.submitRecord(USER_ID, dto);
        return Result.success();
    }

    /**
     * 训练热力图（按年，缺省为今年）
     */
    @GetMapping("/heatmap")
    public Result<TrainingHeatmapVO> getHeatmap(@RequestParam(required = false) Integer year) {
        return Result.success(trainingService.getHeatmap(USER_ID, year));
    }
}
