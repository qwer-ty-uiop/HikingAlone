package com.ty.hikingalone.interfaces.training;

import com.ty.hikingalone.application.train.TrainingService;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.training.dto.command.PlanAbandonDTO;
import com.ty.hikingalone.interfaces.training.dto.command.PlanCreateDTO;
import com.ty.hikingalone.interfaces.training.dto.command.PlanDeleteDTO;
import com.ty.hikingalone.interfaces.training.dto.command.PlanUpdateDTO;
import com.ty.hikingalone.interfaces.training.dto.command.RecordCreateDTO;
import com.ty.hikingalone.interfaces.training.dto.command.RecordUpdateDTO;
import com.ty.hikingalone.interfaces.training.vo.query.TrainingHeatmapVO;
import com.ty.hikingalone.interfaces.training.vo.query.TrainingHomePageVO;
import com.ty.hikingalone.interfaces.training.vo.query.TrainingPlanDetailVO;
import com.ty.hikingalone.interfaces.training.vo.query.TrainingPlanVO;
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
     * 编辑训练计划（含训练项整表替换）
     */
    @PostMapping("/plans/update")
    public Result<Void> updatePlan(@Valid @RequestBody PlanUpdateDTO dto) {
        trainingService.updatePlan(dto);
        return Result.success();
    }

    /**
     * 软删除训练计划（标记已放弃）
     */
    @PostMapping("/plans/abandon")
    public Result<Void> abandonPlan(@Valid @RequestBody PlanAbandonDTO dto) {
        trainingService.abandonPlan(dto);
        return Result.success();
    }

    /**
     * 物理删除训练计划（级联清理其全部记录，不可恢复）
     */
    @PostMapping("/plans/delete")
    public Result<Void> deletePlan(@Valid @RequestBody PlanDeleteDTO dto) {
        trainingService.deletePlan(dto);
        return Result.success();
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
     * 编辑单条打卡记录（仅完成量）
     */
    @PostMapping("/records/update")
    public Result<Void> updateRecord(@Valid @RequestBody RecordUpdateDTO dto) {
        trainingService.updateRecord(USER_ID, dto);
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
