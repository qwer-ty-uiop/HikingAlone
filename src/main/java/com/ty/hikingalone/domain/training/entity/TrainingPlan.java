package com.ty.hikingalone.domain.training.entity;

import com.ty.hikingalone.common.enums.training.TrainingPlanStatusEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练计划领域实体（聚合根）
 * <p>负责自身创建与业务校验，并管理内部的训练项子实体</p>
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrainingPlan {

    private Long id;

    private Long userId;

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * 状态：0已放弃 1进行中 2已完成 3已过期
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 计划内的训练项（子实体，仅创建时由聚合根组装；持久化时由仓储拆分写入）
     */
    private List<TrainingPlanItem> items = new ArrayList<>();

    /**
     * 训练项创建规格：应用层把 DTO 转换成领域参数后交给聚合根工厂
     * record 记录类，纯数据载体、不可变的值对象
     */
    public record ItemSpec(String name, String mode, Integer totalTimes, Integer totalSets, String unit) {
    }

    /**
     * 训练项编辑规格：比 ItemSpec 多一个可选 id（null=新增），供编辑计划时整表替换训练项
     */
    public record ItemEdit(Long id, String name, String mode, Integer totalTimes, Integer totalSets, String unit) {
    }

    /**
     * 工厂方法：业务校验并创建一个"进行中"的训练计划
     * <p>子实体的创建、排序、归属关系在聚合根内部完成，不暴露给应用层</p>
     */
    public static TrainingPlan create(Long userId, String title, String description,
                                      LocalDate startDate, LocalDate endDate,
                                      List<ItemSpec> itemSpecs) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("计划标题不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("周期起止日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        if (itemSpecs == null || itemSpecs.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个训练项");
        }

        TrainingPlan plan = new TrainingPlan();
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setStatus(TrainingPlanStatusEnum.IN_PROGRESS.getCode());

        for (int i = 0; i < itemSpecs.size(); i++) {
            plan.getItems().add(TrainingPlanItem.create(itemSpecs.get(i), i));
        }
        return plan;
    }

    /**
     * 重建工厂：从持久化数据恢复计划，不执行业务校验（数据已合法落库）
     */
    public static TrainingPlan reconstruct(Long id, Long userId, String title, String description,
                                           LocalDate startDate, LocalDate endDate,
                                           Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setStatus(status);
        plan.setCreateTime(createTime);
        plan.setUpdateTime(updateTime);
        return plan;
    }

    /**
     * 周期内是否已过期（超期且未完成）
     */
    public boolean isExpired(LocalDate today) {
        return today.isAfter(endDate);
    }

    /**
     * 把仓储查询出的训练项挂载到聚合根上（回填归属计划id）
     */
    public void attachItems(List<TrainingPlanItem> itemList) {
        this.items = itemList;
        for (TrainingPlanItem item : items) {
            item.setPlanId(id);
        }
    }

    /**
     * 编辑计划：校验并覆盖计划级字段，按编辑规格整表替换训练项（保留已有 id、新增无 id 的项、重排 sort）。
     * <p>返回替换后的训练项列表；被删除的旧项由应用层按「编辑后 id 集合差集」计算并级联清理</p>
     */
    public List<TrainingPlanItem> applyEdit(String title, String description,
                                            LocalDate startDate, LocalDate endDate,
                                            List<ItemEdit> specs) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("计划标题不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("周期起止日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        if (specs == null || specs.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个训练项");
        }

        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;

        List<TrainingPlanItem> newItems = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            ItemEdit spec = specs.get(i);
            TrainingPlanItem item;
            if (spec.id() != null) {
                item = items.stream()
                        .filter(e -> e.getId().equals(spec.id()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("训练项不存在"));
                item.edit(spec.name(), spec.mode(), spec.totalTimes(), spec.totalSets(), spec.unit());
            } else {
                item = TrainingPlanItem.create(
                        new ItemSpec(spec.name(), spec.mode(), spec.totalTimes(), spec.totalSets(), spec.unit()), i);
                item.setPlanId(id);
            }
            item.setSort(i);
            newItems.add(item);
        }
        this.items = newItems;
        return newItems;
    }

    /**
     * 各训练项累计完成值：times模式累加每日次数（totalTimes），sets模式累加每日组数（totalSets）。
     * 数据源为每日汇总表（每训练项每天一行），事件表只保留提交明细
     */
    public Map<Long, Integer> doneItems(List<TrainingRecordDaily> dailies) {
        Map<Long, Integer> doneItems = new HashMap<>();
        for (TrainingPlanItem item : items) {
            int done = dailies.stream()
                    .filter(d -> item.getId().equals(d.getItemId()))
                    .mapToInt(d -> item.isSetsMode()
                            ? (d.getTotalSets() == null ? 0 : d.getTotalSets())
                            : (d.getTotalTimes() == null ? 0 : d.getTotalTimes()))
                    .sum();
            doneItems.put(item.getId(), done);
        }
        return doneItems;
    }

    /**
     * 各训练项达标目标值
     */
    public Map<Long, Integer> itemGoals() {
        Map<Long, Integer> goals = new HashMap<>();
        for (TrainingPlanItem item : items) {
            goals.put(item.getId(), item.targetValue());
        }
        return goals;
    }

    /**
     * 是否全部训练项达标
     */
    public boolean isAllDone(List<TrainingRecordDaily> dailies) {
        if (items.isEmpty()) {
            return false;
        }
        Map<Long, Integer> doneItems = doneItems(dailies);
        return items.stream().allMatch(item -> item.isDone(doneItems.getOrDefault(item.getId(), 0)));
    }

    /**
     * 计划总进度（0~100）：Σ已完成值 / Σ目标值
     */
    public int progress(List<TrainingRecordDaily> dailies) {
        Map<Long, Integer> doneItems = doneItems(dailies);
        int doneTotal = doneItems.values().stream().mapToInt(Integer::intValue).sum();
        int goalTotal = items.stream().mapToInt(TrainingPlanItem::targetValue).sum();
        return goalTotal == 0 ? 0 : Math.min(100, doneTotal * 100 / goalTotal);
    }

    /**
     * 状态流转：仅"进行中"时判定——全部达标→已完成；超期未完成→已过期。
     * 返回新状态（未变化返回 null），是否落库由应用层决定
     */
    public Integer refreshStatus(List<TrainingRecordDaily> dailies, LocalDate today) {
        if (!TrainingPlanStatusEnum.IN_PROGRESS.getCode().equals(status)) {
            return null;
        }
        Integer newStatus = null;
        if (isAllDone(dailies)) {
            newStatus = TrainingPlanStatusEnum.COMPLETED.getCode();
        } else if (isExpired(today)) {
            newStatus = TrainingPlanStatusEnum.EXPIRED.getCode();
        }
        if (newStatus != null) {
            status = newStatus;
        }
        return newStatus;
    }
}
