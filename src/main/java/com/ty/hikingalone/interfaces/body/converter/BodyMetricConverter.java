package com.ty.hikingalone.interfaces.body.converter;

import com.ty.hikingalone.application.body.cmd.BodyMetricRecordCmd;
import com.ty.hikingalone.domain.body.entity.BodyMetric;
import com.ty.hikingalone.interfaces.body.dto.command.BodyMetricRecordDTO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecentVO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricRecordVO;
import com.ty.hikingalone.interfaces.body.vo.query.BodyMetricSummaryVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 身体数据 领域实体/请求体 → VO 转换器
 */
@Component
public class BodyMetricConverter {

    /**
     * 记录请求体 → 应用层命令
     */
    public BodyMetricRecordCmd toRecordCmd(BodyMetricRecordDTO dto) {
        BodyMetricRecordCmd cmd = new BodyMetricRecordCmd();
        cmd.setDate(dto.getDate());
        cmd.setWeightKg(dto.getWeightKg());
        cmd.setRestingHeartRate(dto.getRestingHeartRate());
        cmd.setNote(dto.getNote());
        return cmd;
    }

    /**
     * 领域实体 → 记录 VO
     */
    public BodyMetricRecordVO toRecordVO(BodyMetric metric) {
        BodyMetricRecordVO vo = new BodyMetricRecordVO();
        vo.setId(metric.getId());
        vo.setDate(metric.getRecordDate().toString());
        vo.setWeightKg(metric.getWeightKg());
        vo.setRestingHeartRate(metric.getRestingHeartRate());
        vo.setNote(metric.getNote());
        vo.setUpdateTime(metric.getUpdateTime() == null ? null : metric.getUpdateTime().toString());
        return vo;
    }

    /**
     * 升序记录 + 汇总 → recent VO
     */
    public BodyMetricRecentVO toRecentVO(List<BodyMetric> records, BodyMetricSummaryVO summary, int days) {
        BodyMetricRecentVO vo = new BodyMetricRecentVO();
        vo.setRecords(records.stream().map(this::toRecordVO).toList());
        vo.setSummary(summary);
        vo.setDays(days);
        return vo;
    }
}
