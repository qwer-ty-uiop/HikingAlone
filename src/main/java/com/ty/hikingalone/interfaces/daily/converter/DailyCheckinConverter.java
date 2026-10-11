package com.ty.hikingalone.interfaces.daily.converter;

import com.ty.hikingalone.application.daily.cmd.DailyCheckinCmd;
import com.ty.hikingalone.domain.daily.entity.DailyCheckin;
import com.ty.hikingalone.interfaces.daily.dto.command.DailyCheckinDTO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyCheckinVO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyRecentVO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailySummaryVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日打卡 领域实体/请求体 → VO 转换器
 */
@Component
public class DailyCheckinConverter {

    /**
     * 打卡请求体 → 应用层命令
     */
    public DailyCheckinCmd toCmd(DailyCheckinDTO dto) {
        DailyCheckinCmd cmd = new DailyCheckinCmd();
        cmd.setDate(dto.getDate());
        cmd.setWaterCups(dto.getWaterCups());
        cmd.setSteps(dto.getSteps());
        cmd.setSleepHours(dto.getSleepHours());
        cmd.setSleepQuality(dto.getSleepQuality());
        return cmd;
    }

    /**
     * 领域实体 → 打卡 VO
     */
    public DailyCheckinVO toVO(DailyCheckin checkin) {
        DailyCheckinVO vo = new DailyCheckinVO();
        vo.setId(checkin.getId());
        vo.setDate(checkin.getRecordDate().toString());
        vo.setWaterCups(checkin.getWaterCups());
        vo.setSteps(checkin.getSteps());
        vo.setSleepHours(checkin.getSleepHours());
        vo.setSleepQuality(checkin.getSleepQuality());
        vo.setUpdateTime(checkin.getUpdateTime() == null ? null : checkin.getUpdateTime().toString());
        return vo;
    }

    /**
     * 升序记录 + 汇总 → recent VO
     */
    public DailyRecentVO toRecentVO(List<DailyCheckin> records, DailySummaryVO summary, int days) {
        DailyRecentVO vo = new DailyRecentVO();
        vo.setRecords(records.stream().map(this::toVO).toList());
        vo.setSummary(summary);
        vo.setDays(days);
        return vo;
    }
}
