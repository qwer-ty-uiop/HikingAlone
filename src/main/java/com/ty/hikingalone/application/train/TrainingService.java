package com.ty.hikingalone.application.train;

import com.ty.hikingalone.common.enums.training.TrainingPlanStatusEnum;
import com.ty.hikingalone.controller.training.converter.TrainingConverter;
import com.ty.hikingalone.controller.training.dto.PlanAbandonDTO;
import com.ty.hikingalone.controller.training.dto.PlanCreateDTO;
import com.ty.hikingalone.controller.training.dto.PlanDeleteDTO;
import com.ty.hikingalone.controller.training.dto.PlanUpdateDTO;
import com.ty.hikingalone.controller.training.dto.RecordCreateDTO;
import com.ty.hikingalone.controller.training.dto.RecordUpdateDTO;
import com.ty.hikingalone.controller.training.vo.TrainingHeatmapVO;
import com.ty.hikingalone.controller.training.vo.TrainingHomePageVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanDetailVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanVO;
import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingPlanItem;
import com.ty.hikingalone.domain.training.entity.TrainingRecord;
import com.ty.hikingalone.domain.training.entity.TrainingRecordDaily;
import com.ty.hikingalone.domain.training.repository.TrainingPlanRepository;
import com.ty.hikingalone.domain.training.repository.TrainingRecordDailyRepository;
import com.ty.hikingalone.domain.training.repository.TrainingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 训练模块应用服务：只做 DTO 转换、流程编排与持久化触发，不含业务规则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingPlanRepository planRepository;
    private final TrainingRecordRepository recordRepository;
    private final TrainingRecordDailyRepository dailyRepository;
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
                dto.getStartDate(), dto.getEndDate(),
                dto.getCycleType(), dto.getCycleAnchor(), itemSpecs);
        return planRepository.save(plan);
    }

    /**
     * 计划列表（含每项完成度与计划总进度）
     */
    public List<TrainingPlanVO> listPlans(Long userId) {
        return planRepository.listByUserId(userId).stream().map(plan -> {
            plan.attachItems(planRepository.listItemsByPlanId(plan.getId()));
            List<TrainingRecordDaily> dailies = dailyRepository.listByPlanId(plan.getId());
            List<TrainingRecord> records = recordRepository.listByPlanId(plan.getId());
            applyStatusChange(plan, dailies);
            return converter.toPlanVO(plan, dailies, records);
        }).toList();
    }

    /**
     * 计划详情（含提交记录）
     */
    public TrainingPlanDetailVO getPlanDetail(Long planId) {
        TrainingPlan plan = requirePlan(planId);
        plan.attachItems(planRepository.listItemsByPlanId(planId));
        List<TrainingRecordDaily> dailies = dailyRepository.listByPlanId(planId);
        List<TrainingRecord> records = recordRepository.listByPlanId(planId);
        applyStatusChange(plan, dailies);
        return converter.toPlanDetailVO(plan, dailies, records);
    }

    /**
     * 提交单个训练项当天完成情况：记录组装与校验由 TrainingRecord 工厂完成
     * <p>双写模型：事件表追加一条明细 + 汇总表 upsert 当日行（提交次数+1、按模式累加完成量），同一事务保证原子性；
     * 进度/剩余/热力图等聚合查询读汇总表，事件表保留每次提交的明细供「最近提交」/记录历史展示</p>
     */
    @Transactional
    public void submitRecord(Long userId, RecordCreateDTO dto) {
        TrainingPlan plan = requirePlan(dto.getPlanId());
        plan.attachItems(planRepository.listItemsByPlanId(dto.getPlanId()));
        TrainingPlanItem item = plan.getItems().stream()
                .filter(i -> i.getId().equals(dto.getItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("训练项不存在"));

        LocalDate recordDate = LocalDate.parse(dto.getRecordDate());
        TrainingRecord record = TrainingRecord.submit(userId, dto.getPlanId(), item, recordDate,
                dto.getCompletedSets(), dto.getCompletedTimes());
        recordRepository.insert(record);

        TrainingRecordDaily daily = dailyRepository.findByPlanItemDate(dto.getPlanId(), dto.getItemId(), recordDate);
        if (daily == null) {
            daily = TrainingRecordDaily.from(record);
        } else {
            daily.merge(record);
        }
        dailyRepository.save(daily);

        applyStatusChange(plan, dailyRepository.listByPlanId(dto.getPlanId()));
    }

    /**
     * 编辑训练计划：计划级字段覆盖更新，训练项整表替换（新增/更新/删除），事务保证原子性
     */
    @Transactional
    public void updatePlan(PlanUpdateDTO dto) {
        TrainingPlan plan = requirePlan(dto.getId());
        plan.attachItems(planRepository.listItemsByPlanId(dto.getId()));

        Set<Long> oldItemIds = new HashSet<>();
        for (TrainingPlanItem item : plan.getItems()) {
            oldItemIds.add(item.getId());
        }

        List<TrainingPlan.ItemEdit> specs = dto.getItems().stream()
                .map(i -> new TrainingPlan.ItemEdit(
                        i.getId(), i.getName(), i.getMode(), i.getTotalTimes(), i.getTotalSets(), i.getUnit()))
                .toList();
        List<TrainingPlanItem> newItems = plan.applyEdit(
                dto.getTitle(), dto.getDescription(), dto.getStartDate(), dto.getEndDate(),
                dto.getCycleType(), dto.getCycleAnchor(), specs);

        planRepository.update(plan);

        Set<Long> keptItemIds = new HashSet<>();
        for (TrainingPlanItem item : newItems) {
            if (item.getId() == null) {
                planRepository.saveItem(item, plan.getId());
            } else {
                planRepository.updateItem(item);
            }
            keptItemIds.add(item.getId());
        }

        for (Long itemId : oldItemIds) {
            if (!keptItemIds.contains(itemId)) {
                recordRepository.deleteByItemId(itemId);
                dailyRepository.deleteByItemId(itemId);
                planRepository.deleteItem(itemId);
            }
        }

        applyStatusChange(plan, dailyRepository.listByPlanId(dto.getId()));
    }

    /**
     * 放弃计划：仅「进行中」可放弃，置状态为已放弃（保留历史记录与热力图）
     * <p>状态机：ABANDONED 只能由 IN_PROGRESS 经用户操作进入；已完成/已过期/已放弃 拒绝</p>
     */
    public void abandonPlan(PlanAbandonDTO dto) {
        TrainingPlan plan = requirePlan(dto.getId());
        if (!TrainingPlanStatusEnum.IN_PROGRESS.getCode().equals(plan.getStatus())) {
            throw new IllegalArgumentException("仅进行中的计划可以放弃");
        }
        planRepository.updateStatus(dto.getId(), TrainingPlanStatusEnum.ABANDONED.getCode());
    }

    /**
     * 物理删除计划：级联清理事件表、汇总表与训练项，计划与其历史记录从数据库整体移除，事务保证原子性
     */
    @Transactional
    public void deletePlan(PlanDeleteDTO dto) {
        requirePlan(dto.getId());
        recordRepository.deleteByPlanId(dto.getId());
        dailyRepository.deleteByPlanId(dto.getId());
        planRepository.delete(dto.getId());
    }

    /**
     * 编辑单条打卡记录：仅重设完成量，事件表刷新 updateTime、汇总表按差值调整，事务保证原子性
     */
    @Transactional
    public void updateRecord(Long userId, RecordUpdateDTO dto) {
        TrainingRecord record = recordRepository.findById(dto.getId());
        if (record == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        TrainingPlan plan = requirePlan(record.getPlanId());
        plan.attachItems(planRepository.listItemsByPlanId(record.getPlanId()));
        TrainingPlanItem item = plan.getItems().stream()
                .filter(i -> i.getId().equals(record.getItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("训练项不存在"));

        int oldSets = record.getCompletedSets() == null ? 0 : record.getCompletedSets();
        int oldTimes = record.getCompletedTimes() == null ? 0 : record.getCompletedTimes();

        record.edit(item, dto.getCompletedSets(), dto.getCompletedTimes());
        recordRepository.update(record);

        int deltaSets = record.getCompletedSets() - oldSets;
        int deltaTimes = record.getCompletedTimes() - oldTimes;

        TrainingRecordDaily daily = dailyRepository.findByPlanItemDate(
                record.getPlanId(), record.getItemId(), record.getRecordDate());
        if (daily == null) {
            daily = TrainingRecordDaily.from(record);
        } else {
            daily.adjust(deltaSets, deltaTimes);
        }
        dailyRepository.save(daily);

        applyStatusChange(plan, dailyRepository.listByPlanId(record.getPlanId()));
    }

    /**
     * 训练热力图：按年统计每天提交次数（读每日汇总表的 commitCount）
     */
    public TrainingHeatmapVO getHeatmap(Long userId, Integer year) {
        int targetYear = year == null ? LocalDate.now().getYear() : year;
        List<TrainingRecordDaily> dailies = dailyRepository.listByUserAndDateRange(
                userId, LocalDate.of(targetYear, 1, 1), LocalDate.of(targetYear, 12, 31));
        return converter.toHeatmapVO(targetYear, dailies);
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
     * 状态流转判定在聚合根（refreshStatus），应用层只负责把结果落库。
     * 周期计划只取当前周期内的汇总行参与达标判定（周期外历史不影响本期状态）
     */
    private void applyStatusChange(TrainingPlan plan, List<TrainingRecordDaily> dailies) {
        Integer newStatus = plan.refreshStatus(plan.currentPeriodDailies(dailies, LocalDate.now()), LocalDate.now());
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
