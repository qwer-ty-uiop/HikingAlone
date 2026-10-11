package com.ty.hikingalone.interfaces.goal.converter;

import com.ty.hikingalone.application.goal.cmd.GoalCreateCmd;
import com.ty.hikingalone.application.goal.cmd.GoalIdCmd;
import com.ty.hikingalone.application.goal.cmd.GoalUpdateCmd;
import com.ty.hikingalone.common.enums.goal.GoalMetricEnum;
import com.ty.hikingalone.domain.goal.entity.HikeGoal;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalCreateDTO;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalIdDTO;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalUpdateDTO;
import com.ty.hikingalone.interfaces.goal.vo.query.GoalVO;
import org.springframework.stereotype.Component;

/**
 * 目标 领域实体/请求体 → VO 转换器
 */
@Component
public class GoalConverter {

    public GoalCreateCmd toCreateCmd(GoalCreateDTO dto) {
        GoalCreateCmd cmd = new GoalCreateCmd();
        cmd.setMetric(dto.getMetric());
        cmd.setTargetValue(dto.getTargetValue());
        cmd.setYear(dto.getYear());
        cmd.setMonth(dto.getMonth());
        return cmd;
    }

    public GoalUpdateCmd toUpdateCmd(GoalUpdateDTO dto) {
        GoalUpdateCmd cmd = new GoalUpdateCmd();
        cmd.setId(dto.getId());
        cmd.setTargetValue(dto.getTargetValue());
        cmd.setStatus(dto.getStatus());
        return cmd;
    }

    public GoalIdCmd toIdCmd(GoalIdDTO dto) {
        GoalIdCmd cmd = new GoalIdCmd();
        cmd.setId(dto.getId());
        return cmd;
    }

    public GoalVO toVO(HikeGoal goal, GoalVO.Progress progress) {
        GoalVO vo = new GoalVO();
        vo.setId(goal.getId());
        vo.setMetric(goal.getMetric());
        vo.setMetricLabel(GoalMetricEnum.fromCode(goal.getMetric()).getDesc());
        vo.setTargetValue(goal.getTargetValue());
        vo.setPeriodType(goal.getPeriodType());
        vo.setYear(goal.getYear());
        vo.setMonth(goal.getMonth());
        vo.setStatus(goal.getStatus());
        vo.setProgress(progress);
        return vo;
    }
}
