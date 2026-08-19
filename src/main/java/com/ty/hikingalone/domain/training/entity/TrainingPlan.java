package com.ty.hikingalone.domain.training.entity;

import com.ty.hikingalone.common.enums.training.TrainingCycleTypeEnum;
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
     * 周期类型：0不重复 1每天 2每周 3每月 4每年（0=非周期，聚合统计全部记录，老行为）
     */
    private Integer cycleType;

    /**
     * 周期锚点：每周=星期(1周一~7周日)；每月=日(1~31)；每年=月*100+日(如815=8月15日)；null=默认锚点(周一/1号/1月1日)
     */
    private Integer cycleAnchor;

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
     * 一个周期的起止日期（含两端）。非周期计划无周期，聚合统计不过滤
     */
    public record Period(LocalDate start, LocalDate end) {
    }

    /**
     * 工厂方法：业务校验并创建一个"进行中"的训练计划
     * <p>子实体的创建、排序、归属关系在聚合根内部完成，不暴露给应用层</p>
     */
    public static TrainingPlan create(Long userId, String title, String description,
                                      LocalDate startDate, LocalDate endDate,
                                      Integer cycleType, Integer cycleAnchor,
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
        validateCycle(cycleType, cycleAnchor);

        TrainingPlan plan = new TrainingPlan();
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setCycleType(cycleType == null ? TrainingCycleTypeEnum.NONE.getCode() : cycleType);
        // 不重复的周期无需锚点，统一置空
        plan.setCycleAnchor(TrainingCycleTypeEnum.NONE.getCode().equals(plan.getCycleType()) ? null : cycleAnchor);
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
                                           Integer cycleType, Integer cycleAnchor,
                                           Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setUserId(userId);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setStartDate(startDate);
        plan.setEndDate(endDate);
        plan.setCycleType(cycleType);
        plan.setCycleAnchor(cycleAnchor);
        plan.setStatus(status);
        plan.setCreateTime(createTime);
        plan.setUpdateTime(updateTime);
        return plan;
    }

    /**
     * 周期字段校验：周期类型合法且锚点取值在对应粒度范围内（创建与编辑共用）。
     * 每天与不重复不允许带锚点；每周 1~7；每月 1~31；每年 月*100+日（月 1~12、日 1~31）
     */
    private static void validateCycle(Integer cycleType, Integer cycleAnchor) {
        if (cycleType == null || TrainingCycleTypeEnum.NONE.getCode().equals(cycleType)) {
            if (cycleAnchor != null) {
                throw new IllegalArgumentException("不重复的周期不需要锚点");
            }
            return;
        }
        TrainingCycleTypeEnum type = TrainingCycleTypeEnum.fromCode(cycleType);
        if (cycleAnchor == null) {
            return; // 空锚点 = 使用默认锚点（每周周一 / 每月1号 / 每年1月1日）
        }
        switch (type) {
            case DAILY -> throw new IllegalArgumentException("每天周期不需要锚点");
            case WEEKLY -> {
                if (cycleAnchor < 1 || cycleAnchor > 7) {
                    throw new IllegalArgumentException("每周周期锚点取值为1~7（1周一~7周日）");
                }
            }
            case MONTHLY -> {
                if (cycleAnchor < 1 || cycleAnchor > 31) {
                    throw new IllegalArgumentException("每月周期锚点取值为1~31（几号重置）");
                }
            }
            case YEARLY -> {
                int month = cycleAnchor / 100;
                int day = cycleAnchor % 100;
                if (month < 1 || month > 12 || day < 1 || day > 31) {
                    throw new IllegalArgumentException("每年周期锚点格式为月*100+日（如815=8月15日）");
                }
            }
            default -> throw new IllegalArgumentException("周期类型不合法");
        }
    }

    /**
     * 是否周期计划（每天/每周/每月/每年）
     */
    public boolean hasCycle() {
        return cycleType != null && !TrainingCycleTypeEnum.NONE.getCode().equals(cycleType);
    }

    /**
     * 计算某天所在的当前周期（含两端）。
     * <p>按自然日历对齐，与计划开始日期无关；非周期计划返回 null（聚合统计不过滤日期）</p>
     */
    public Period currentPeriod(LocalDate today) {
        if (!hasCycle()) {
            return null;
        }
        TrainingCycleTypeEnum type = TrainingCycleTypeEnum.fromCode(cycleType);
        return switch (type) {
            case DAILY -> new Period(today, today);
            case WEEKLY -> weeklyPeriod(today);
            case MONTHLY -> monthlyPeriod(today);
            case YEARLY -> yearlyPeriod(today);
            default -> null;
        };
    }

    /**
     * 每周周期：锚点星期（缺省周一）所在自然周，周一~周日或锚点日起第 6 天
     */
    private Period weeklyPeriod(LocalDate today) {
        int anchor = cycleAnchor == null ? 1 : cycleAnchor;
        int todayDow = today.getDayOfWeek().getValue(); // 1=周一 ... 7=周日
        int offset = Math.floorMod(todayDow - anchor, 7);
        LocalDate start = today.minusDays(offset);
        return new Period(start, start.plusDays(6));
    }

    /**
     * 每月周期：锚点日（缺省1号）为周期起始；今日早于锚点日则从上月锚点日起算，超月长取月末
     */
    private Period monthlyPeriod(LocalDate today) {
        int anchor = cycleAnchor == null ? 1 : cycleAnchor;
        LocalDate start;
        if (today.getDayOfMonth() >= anchor) {
            start = clampDate(today.getYear(), today.getMonthValue(), anchor);
        } else {
            LocalDate prev = today.minusMonths(1);
            start = clampDate(prev.getYear(), prev.getMonthValue(), anchor);
        }
        return new Period(start, start.plusMonths(1).minusDays(1));
    }

    /**
     * 每年周期：锚点月日（缺省1月1日）为周期起始；今日早于锚点则从去年锚点日起算，2月30日等无效日期取当月月末
     */
    private Period yearlyPeriod(LocalDate today) {
        int anchorMonth = 1;
        int anchorDay = 1;
        if (cycleAnchor != null) {
            anchorMonth = cycleAnchor / 100;
            anchorDay = cycleAnchor % 100;
        }
        int year = today.getYear();
        LocalDate startThisYear = clampDate(year, anchorMonth, anchorDay);
        LocalDate start = today.isBefore(startThisYear)
                ? clampDate(year - 1, anchorMonth, anchorDay)
                : startThisYear;
        return new Period(start, start.plusYears(1).minusDays(1));
    }

    /**
     * 构造合法日期：月份/日越界时钳制到当月最后一天（如 2月30日 → 2月28/29日）
     */
    private static LocalDate clampDate(int year, int month, int day) {
        int m = Math.clamp(month, 1, 12);
        int d = Math.clamp(day, 1, LocalDate.of(year, m, 1).lengthOfMonth());
        return LocalDate.of(year, m, d);
    }

    /**
     * 聚合统计用的每日汇总行：周期计划只取当前周期内的行（周期之外的记录保留在库中但不计入本期进度）；
     * 非周期计划不做过滤，统计全部记录
     */
    public List<TrainingRecordDaily> currentPeriodDailies(List<TrainingRecordDaily> dailies, LocalDate today) {
        Period period = currentPeriod(today);
        if (period == null) {
            return dailies;
        }
        return dailies.stream()
                .filter(d -> !d.getRecordDate().isBefore(period.start()) && !d.getRecordDate().isAfter(period.end()))
                .toList();
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
                                            Integer cycleType, Integer cycleAnchor,
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
        validateCycle(cycleType, cycleAnchor);

        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cycleType = cycleType == null ? TrainingCycleTypeEnum.NONE.getCode() : cycleType;
        this.cycleAnchor = TrainingCycleTypeEnum.NONE.getCode().equals(this.cycleType) ? null : cycleAnchor;

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
     * 状态流转：按计划类型分发——周期计划永续进行中，非周期计划按达标/超期自动流转。
     * 返回新状态（未变化返回 null），是否落库由应用层决定
     */
    public Integer refreshStatus(List<TrainingRecordDaily> dailies, LocalDate today) {
        return hasCycle() ? refreshCycleStatus() : refreshOneTimeStatus(dailies, today);
    }

    /**
     * 周期计划状态流转：不因时间自动流转（每个新周期自动重置本期目标），计划始终处于"进行中"；
     * 历史遗留的 已完成/已过期 周期计划读取时自动恢复为 进行中；已放弃是终态，不恢复。
     * 返回新状态（未变化返回 null）
     */
    private Integer refreshCycleStatus() {
        if (TrainingPlanStatusEnum.ABANDONED.getCode().equals(status)
                || TrainingPlanStatusEnum.IN_PROGRESS.getCode().equals(status)) {
            return null;
        }
        status = TrainingPlanStatusEnum.IN_PROGRESS.getCode();
        return TrainingPlanStatusEnum.IN_PROGRESS.getCode();
    }

    /**
     * 非周期计划状态流转：仅"进行中"时判定——全部达标→已完成，超期未完成→已过期。
     * 返回新状态（未变化返回 null）
     */
    private Integer refreshOneTimeStatus(List<TrainingRecordDaily> dailies, LocalDate today) {
        if (!TrainingPlanStatusEnum.IN_PROGRESS.getCode().equals(status)) {
            return null;
        }
        Integer newStatus = isAllDone(dailies)
                ? TrainingPlanStatusEnum.COMPLETED.getCode()
                : (isExpired(today) ? TrainingPlanStatusEnum.EXPIRED.getCode() : null);
        if (newStatus != null) {
            status = newStatus;
        }
        return newStatus;
    }
}
