package com.ty.hikingalone.domain.training.entity;

import com.ty.hikingalone.common.enums.training.TrainingModeEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 训练计划项领域实体（聚合根 TrainingPlan 的子实体）
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrainingPlanItem {

    private Long id;

    private Long planId;

    private String name;

    /**
     * 制定模式：times=按次数, sets=按次数+组数
     */
    private String mode;

    /**
     * 目标次数（times模式=总次数；sets模式=每组次数）
     */
    private Integer totalTimes;

    /**
     * 目标组数（仅sets模式）
     */
    private Integer totalSets;

    private String unit;

    private Integer sort;

    /**
     * 工厂方法：校验并创建一个训练项
     * <p>由聚合根 TrainingPlan.create 调用，planId 在持久化时由仓储统一赋值</p>
     */
    public static TrainingPlanItem create(TrainingPlan.ItemSpec spec, int sort) {
        if (spec.name() == null || spec.name().isBlank()) {
            throw new IllegalArgumentException("训练项名称不能为空");
        }
        boolean isSetsMode = TrainingModeEnum.SETS.getCode().equals(spec.mode());
        boolean isTimesMode = TrainingModeEnum.TIMES.getCode().equals(spec.mode());

        if (!isTimesMode && !isSetsMode) {
            throw new IllegalArgumentException("训练项制定模式不合法");
        }
        if (spec.totalTimes() == null || spec.totalTimes() <= 0) {
            throw new IllegalArgumentException("目标次数必须大于0");
        }
        if (isSetsMode && (spec.totalSets() == null || spec.totalSets() <= 0)) {
            throw new IllegalArgumentException("sets模式必须填写目标组数");
        }

        TrainingPlanItem item = new TrainingPlanItem();
        item.setName(spec.name());
        item.setMode(spec.mode());
        item.setTotalTimes(spec.totalTimes());
        item.setTotalSets(spec.totalSets());
        item.setUnit(spec.unit());
        item.setSort(sort);
        return item;
    }

    /**
     * 重建工厂：从持久化数据恢复训练项，不执行业务校验（数据已合法落库）
     */
    public static TrainingPlanItem reconstruct(Long id, Long planId, String name, String mode, Integer totalTimes, Integer totalSets, String unit, Integer sort) {
        TrainingPlanItem item = new TrainingPlanItem();
        item.setId(id);
        item.setPlanId(planId);
        item.setName(name);
        item.setMode(mode);
        item.setTotalTimes(totalTimes);
        item.setTotalSets(totalSets);
        item.setUnit(unit);
        item.setSort(sort);
        return item;
    }

    /**
     * 是否按次数+组数模式
     */
    public boolean isSetsMode() {
        return "sets".equals(mode);
    }

    /**
     * 计算达标的目标值：
     * times模式=总次数；sets模式=总组数
     */
    public int targetValue() {
        return isSetsMode() ? totalSets : totalTimes;
    }

    /**
     * 按累计完成值判断是否达标
     */
    public boolean isDone(int doneValue) {
        return targetValue() > 0 && doneValue >= targetValue();
    }
}
