package com.ty.hikingalone.application.train;

import com.ty.hikingalone.controller.training.converter.TrainingConverter;
import com.ty.hikingalone.controller.training.dto.PlanCreateDTO;
import com.ty.hikingalone.controller.training.dto.RecordCreateDTO;
import com.ty.hikingalone.controller.training.vo.TrainingHeatmapVO;
import com.ty.hikingalone.controller.training.vo.TrainingHomePageVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanDetailVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanVO;
import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingPlanItem;
import com.ty.hikingalone.domain.training.entity.TrainingRecord;
import com.ty.hikingalone.domain.training.repository.TrainingPlanRepository;
import com.ty.hikingalone.domain.training.repository.TrainingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 训练模块应用服务：只做 DTO 转换、流程编排与持久化触发，不含业务规则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingPlanRepository planRepository;
    private final TrainingRecordRepository recordRepository;
    private final TrainingConverter converter;

    /**
     * 制定训练计划：应用层只做 DTO→领域参数转换，创建与校验交给聚合根工厂
     */
    public Long createPlan(Long userId, PlanCreateDTO dto) {
        List<TrainingPlan.ItemSpec> itemSpecs = dto.getItems().stream()
                .map(i -> new TrainingPlan.ItemSpec(
                        i.getName(), i.getMode(), i.getTotalTimes(), i.getTotalSets(), i.getUnit()))
                .toList();
        TrainingPlan plan = TrainingPlan.create(
                userId, dto.getTitle(), dto.getDescription(),
                dto.getStartDate(), dto.getEndDate(), itemSpecs);
        return planRepository.save(plan);
    }

    /**
     * 计划列表（含每项完成度与计划总进度）
     */
    public List<TrainingPlanVO> listPlans(Long userId) {
        return planRepository.listByUserId(userId).stream().map(plan -> {
            plan.attachItems(planRepository.listItemsByPlanId(plan.getId()));
            List<TrainingRecord> records = recordRepository.listByPlanId(plan.getId());
            applyStatusChange(plan, records);
            return converter.toPlanVO(plan, records);
        }).toList();
    }

    /**
     * 计划详情（含每日提交记录）
     */
    public TrainingPlanDetailVO getPlanDetail(Long planId) {
        TrainingPlan plan = requirePlan(planId);
        plan.attachItems(planRepository.listItemsByPlanId(planId));
        List<TrainingRecord> records = recordRepository.listByPlanId(planId);
        applyStatusChange(plan, records);
        return converter.toPlanDetailVO(plan, records);
    }

    /**
     * 提交单个训练项当天完成情况：记录组装与校验由 TrainingRecord 工厂完成
     * <p>同一天重复提交走合并累加（每次提交是本次完成量，而非覆盖当天值）</p>
     */
    public void submitRecord(Long userId, RecordCreateDTO dto) {
        TrainingPlan plan = requirePlan(dto.getPlanId());
        plan.attachItems(planRepository.listItemsByPlanId(dto.getPlanId()));
        TrainingPlanItem item = plan.getItems().stream()
                .filter(i -> i.getId().equals(dto.getItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("训练项不存在"));

        LocalDate recordDate = LocalDate.parse(dto.getRecordDate());
        TrainingRecord existing = recordRepository.findByPlanItemDate(dto.getPlanId(), dto.getItemId(), recordDate);
        TrainingRecord record = existing != null
                ? mergeInto(existing, item, dto)
                : TrainingRecord.submit(userId, dto.getPlanId(), item, recordDate,
                        dto.getCompletedSets(), dto.getCompletedTimes());
        recordRepository.saveOrUpdate(record);

        applyStatusChange(plan, recordRepository.listByPlanId(dto.getPlanId()));
    }

    /**
     * 当天已有记录：本次提交量累加进原记录，返回原记录（带 id，走更新）
     */
    private TrainingRecord mergeInto(TrainingRecord existing, TrainingPlanItem item, RecordCreateDTO dto) {
        existing.mergeSubmit(item, dto.getCompletedSets(), dto.getCompletedTimes());
        return existing;
    }

    /**
     * 训练热力图：按年统计每天提交的记录条数
     */
    public TrainingHeatmapVO getHeatmap(Long userId, Integer year) {
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        List<TrainingRecord> records = recordRepository.listByUserAndDateRange(
                userId, LocalDate.of(targetYear, 1, 1), LocalDate.of(targetYear, 12, 31));
        return converter.toHeatmapVO(targetYear, records);
    }

    /**
     * 训练首页聚合：近期计划 + 今年热力图
     */
    public TrainingHomePageVO getHomePage(Long userId) {
        TrainingHomePageVO vo = new TrainingHomePageVO();
        vo.setPlans(listPlans(userId));
        vo.setHeatmap(getHeatmap(userId, LocalDate.now().getYear()));
        return vo;
    }

    /**
     * 状态流转判定在聚合根（refreshStatus），应用层只负责把结果落库
     */
    private void applyStatusChange(TrainingPlan plan, List<TrainingRecord> records) {
        Integer newStatus = plan.refreshStatus(records, LocalDate.now());
        if (newStatus != null) {
            planRepository.updateStatus(plan.getId(), newStatus);
        }
    }

    private TrainingPlan requirePlan(Long planId) {
        TrainingPlan plan = planRepository.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("训练计划不存在");
        }
        return plan;
    }
}
