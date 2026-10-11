package com.ty.hikingalone.interfaces.journal.converter;

import com.ty.hikingalone.application.journal.cmd.JournalCmd;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.interfaces.journal.dto.command.JournalSaveDTO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalDetailVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalSummaryVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 徒步日记 领域实体/请求体 → VO 转换器
 */
@Component
public class JournalConverter {

    /** 列表正文预览长度 */
    private static final int PREVIEW_LENGTH = 120;

    /**
     * 保存请求体 → 应用层命令
     */
    public JournalCmd toCmd(JournalSaveDTO dto) {
        JournalCmd cmd = new JournalCmd();
        cmd.setTitle(dto.getTitle());
        cmd.setHikeDate(dto.getHikeDate());
        cmd.setRouteId(dto.getRouteId());
        cmd.setDistanceKm(dto.getDistanceKm());
        cmd.setDurationMin(dto.getDurationMin());
        cmd.setElevationGain(dto.getElevationGain());
        cmd.setDifficulty(dto.getDifficulty());
        cmd.setMood(dto.getMood());
        cmd.setWeather(dto.getWeather());
        cmd.setCompanions(dto.getCompanions());
        cmd.setCost(dto.getCost());
        cmd.setContent(dto.getContent());
        return cmd;
    }

    /**
     * 领域实体 → 列表 VO（routeName 从批量查询的映射取，缺省 null）
     */
    public JournalVO toVO(HikeJournal journal, Map<Long, String> routeNames) {
        JournalVO vo = new JournalVO();
        vo.setId(journal.getId());
        vo.setTitle(journal.getTitle());
        vo.setHikeDate(journal.getHikeDate().toString());
        vo.setRouteId(journal.getRouteId());
        vo.setRouteName(journal.getRouteId() == null ? null : routeNames.get(journal.getRouteId()));
        vo.setDistanceKm(journal.getDistanceKm());
        vo.setDurationMin(journal.getDurationMin());
        vo.setElevationGain(journal.getElevationGain());
        vo.setDifficulty(journal.getDifficulty());
        vo.setMood(journal.getMood());
        vo.setWeather(journal.getWeather());
        vo.setCompanions(journal.getCompanions());
        vo.setCost(journal.getCost());
        vo.setContentPreview(preview(journal.getContent()));
        return vo;
    }

    /**
     * 批量转换
     */
    public List<JournalVO> toVOList(List<HikeJournal> journals, Map<Long, String> routeNames) {
        return journals.stream().map(j -> toVO(j, routeNames)).toList();
    }

    /**
     * 领域实体 → 详情 VO（完整正文）
     */
    public JournalDetailVO toDetailVO(HikeJournal journal, Map<Long, String> routeNames) {
        JournalDetailVO vo = new JournalDetailVO();
        vo.setId(journal.getId());
        vo.setTitle(journal.getTitle());
        vo.setHikeDate(journal.getHikeDate().toString());
        vo.setRouteId(journal.getRouteId());
        vo.setRouteName(journal.getRouteId() == null ? null : routeNames.get(journal.getRouteId()));
        vo.setDistanceKm(journal.getDistanceKm());
        vo.setDurationMin(journal.getDurationMin());
        vo.setElevationGain(journal.getElevationGain());
        vo.setDifficulty(journal.getDifficulty());
        vo.setMood(journal.getMood());
        vo.setWeather(journal.getWeather());
        vo.setCompanions(journal.getCompanions());
        vo.setCost(journal.getCost());
        vo.setContent(journal.getContent());
        vo.setCreateTime(journal.getCreateTime() == null ? null : journal.getCreateTime().toString());
        vo.setUpdateTime(journal.getUpdateTime() == null ? null : journal.getUpdateTime().toString());
        return vo;
    }

    /**
     * 正文预览：按字符截断（中文等宽处理，超长追加省略号）
     */
    private String preview(String content) {
        if (content == null || content.length() <= PREVIEW_LENGTH) {
            return content;
        }
        return content.substring(0, PREVIEW_LENGTH) + "…";
    }
}
