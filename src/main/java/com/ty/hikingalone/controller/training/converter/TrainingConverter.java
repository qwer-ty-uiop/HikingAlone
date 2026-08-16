package com.ty.hikingalone.controller.training.converter;

import com.ty.hikingalone.controller.training.vo.TrainingHeatmapVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanDetailVO;
import com.ty.hikingalone.controller.training.vo.TrainingPlanVO;
import com.ty.hikingalone.domain.training.entity.TrainingPlan;
import com.ty.hikingalone.domain.training.entity.TrainingRecord;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 训练领域实体 → 视图对象 转换器
 */
@Component
public class TrainingConverter {

    /**
     * 计划（含已挂载训练项）→ 列表VO
     */
    public TrainingPlanVO toPlanVO(TrainingPlan plan, List<TrainingRecord> records) {
        TrainingPlanVO vo = new TrainingPlanVO();
        vo.setId(plan.getId());
        vo.setTitle(plan.getTitle());
        vo.setDescription(plan.getDescription());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());

        Map<Long, Integer> doneMap = plan.doneItems(records);
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
        vo.setProgress(plan.progress(records));

        vo.setRecords(records.stream().map(r -> {
            TrainingPlanVO.RecordVO recordVO = new TrainingPlanVO.RecordVO();
            recordVO.setDate(r.getRecordDate().toString());
            recordVO.setItemId(r.getItemId());
            recordVO.setCompletedSets(r.getCompletedSets());
            recordVO.setCompletedTimes(r.getCompletedTimes());
            return recordVO;
        }).toList());

        return vo;
    }

    /**
     * 计划 → 详情VO（在列表VO基础上增加每日记录）
     */
    public TrainingPlanDetailVO toPlanDetailVO(TrainingPlan plan, List<TrainingRecord> records) {
        TrainingPlanDetailVO vo = new TrainingPlanDetailVO();
        TrainingPlanVO base = toPlanVO(plan, records);
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
     * 每日记录 → 热力图VO（按日期统计记录条数）
     */
    public TrainingHeatmapVO toHeatmapVO(Integer year, List<TrainingRecord> records) {
        TrainingHeatmapVO vo = new TrainingHeatmapVO();
        vo.setYear(year);
        vo.setTotalCount(records.size());

        // 按日期分组，统计当天提交的记录条数
        Map<String, Long> countByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordDate().toString(), LinkedHashMap::new, Collectors.counting()));

        List<TrainingHeatmapVO.DayVO> days = countByDate.entrySet().stream().map(e -> {
            TrainingHeatmapVO.DayVO day = new TrainingHeatmapVO.DayVO();
            day.setDate(e.getKey());
            day.setCount(e.getValue().intValue());
            return day;
        }).toList();
        vo.setDays(days);
        return vo;
    }
}
