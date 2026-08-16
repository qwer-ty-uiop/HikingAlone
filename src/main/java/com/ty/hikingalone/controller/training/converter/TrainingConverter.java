package com.ty.hikingalone.controller.training.converter;

import com.ty.hikingalone.controller.training.vo.TrainingHeatmapVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanDetailVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanVO;
import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingRecord;
import com.ty.hikingalone.domain.training.entity.TrainingRecordDaily;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 训练领域实体 → 视图对象 转换器
 */
@Component
public class TrainingConverter {

    /**
     * 计划（含已挂载训练项）→ 列表VO
     * <p>完成度/进度读每日汇总表（dailies，每训练项每天一行）；records 读事件表（events，每次提交一条，供「最近提交」展示）</p>
     */
    public TrainingPlanVO toPlanVO(TrainingPlan plan, List<TrainingRecordDaily> dailies, List<TrainingRecord> events) {
        TrainingPlanVO vo = new TrainingPlanVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setDescription(plan.getDescription());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());

        Map<Long, Integer> doneMap = plan.doneItems(dailies);
        Map<Long, Integer> goalMap = plan.itemGoals();
        List<TrainingPlanVO.ItemVO> itemVOs = plan.getItems().stream().map(item -> {
            TrainingPlanVO.ItemVO itemVO = new TrainingPlanVO.ItemVO();
            itemVO.setId(item.getId());
            itemVO.setName(item.getName());
            itemVO.setMode(item.getMode());
            itemVO.setTotalTimes(item.getTotalTimes());
            itemVO.setTotalSets(item.getTotalSets());
            itemVO.setUnit(item.getUnit());
            int done = doneMap.getOrDefault(item.getId(), 0);
            itemVO.setDoneValue(done);
            itemVO.setRemainValue(item.remainValue(done));
            itemVO.setDone(item.isDone(done));
            return itemVO;
        }).toList();
        vo.setItems(itemVOs);
        vo.setProgress(plan.progress(dailies));

        vo.setRecords(events.stream().map(r -> {
            TrainingPlanVO.RecordVO recordVO = new TrainingPlanVO.RecordVO();
            recordVO.setId(r.getId());
            recordVO.setDate(r.getRecordDate().toString());
            recordVO.setItemId(r.getItemId());
            recordVO.setCompletedSets(r.getCompletedSets());
            recordVO.setCompletedTimes(r.getCompletedTimes());
            recordVO.setCreateTime(r.getCreateTime() == null ? null : r.getCreateTime().withNano(0).toString());
            recordVO.setUpdateTime(r.getUpdateTime() == null ? null : r.getUpdateTime().withNano(0).toString());
            return recordVO;
        }).toList());

        return vo;
    }

    /**
     * 计划 → 详情VO（在列表VO基础上增加每日提交记录）
     */
    public TrainingPlanDetailVO toPlanDetailVO(TrainingPlan plan, List<TrainingRecordDaily> dailies, List<TrainingRecord> events) {
        TrainingPlanDetailVO vo = new TrainingPlanDetailVO();
        TrainingPlanVO base = toPlanVO(plan, dailies, events);
        vo.setId(base.getId());
        vo.setTitle(base.getTitle());
        vo.setDescription(base.getDescription());
        vo.setStartDate(base.getStartDate());
        vo.setEndDate(base.getEndDate());
        vo.setStatus(base.getStatus());
        vo.setProgress(base.getProgress());
        vo.setItems(base.getItems());
        vo.setRecords(base.getRecords());
        return vo;
    }

    /**
     * 每日汇总 → 热力图VO（读汇总表的 commitCount：当天提交次数）
     */
    public TrainingHeatmapVO toHeatmapVO(Integer year, List<TrainingRecordDaily> dailies) {
        TrainingHeatmapVO vo = new TrainingHeatmapVO();
        vo.setYear(year);
        vo.setTotalCount(dailies.stream()
                .mapToInt(d -> d.getCommitCount() == null ? 0 : d.getCommitCount())
                .sum());

        // 按日期分组：同一天可能多个训练项各自有汇总行，count 相加
        Map<String, Integer> countByDate = new LinkedHashMap<>();
        for (TrainingRecordDaily daily : dailies) {
            countByDate.merge(daily.getRecordDate().toString(),
                    daily.getCommitCount() == null ? 0 : daily.getCommitCount(), Integer::sum);
        }

        List<TrainingHeatmapVO.DayVO> days = countByDate.entrySet().stream().map(e -> {
            TrainingHeatmapVO.DayVO day = new TrainingHeatmapVO.DayVO();
            day.setDate(e.getKey());
            day.setCount(e.getValue());
            return day;
        }).toList();
        vo.setDays(days);
        return vo;
    }
}
