package com.ty.hikingalone.interfaces.wish.converter;

import com.ty.hikingalone.application.wish.cmd.WishCmd;
import com.ty.hikingalone.application.wish.cmd.WishCompleteCmd;
import com.ty.hikingalone.application.wish.cmd.WishIdCmd;
import com.ty.hikingalone.domain.wish.entity.HikeWish;
import com.ty.hikingalone.interfaces.wish.dto.command.WishCompleteDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishIdDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishSaveDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishUpdateDTO;
import com.ty.hikingalone.interfaces.wish.vo.query.WishVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 徒步心愿 领域实体/请求体 → VO 转换器
 */
@Component
public class WishConverter {

    public WishCmd toCmd(WishSaveDTO dto) {
        WishCmd cmd = new WishCmd();
        cmd.setTitle(dto.getTitle());
        cmd.setLocation(dto.getLocation());
        cmd.setExpectedDate(dto.getExpectedDate());
        cmd.setPriority(dto.getPriority());
        cmd.setNote(dto.getNote());
        return cmd;
    }

    public WishCmd toCmd(WishUpdateDTO dto) {
        WishCmd cmd = toCmd((WishSaveDTO) dto);
        cmd.setId(dto.getId());
        cmd.setStatus(dto.getStatus());
        return cmd;
    }

    public WishCompleteCmd toCompleteCmd(WishCompleteDTO dto) {
        WishCompleteCmd cmd = new WishCompleteCmd();
        cmd.setId(dto.getId());
        cmd.setDoneDate(dto.getDoneDate());
        cmd.setJournalId(dto.getJournalId());
        return cmd;
    }

    public WishIdCmd toIdCmd(WishIdDTO dto) {
        WishIdCmd cmd = new WishIdCmd();
        cmd.setId(dto.getId());
        return cmd;
    }

    public WishVO toVO(HikeWish wish, Map<Long, String> journalTitles) {
        WishVO vo = new WishVO();
        vo.setId(wish.getId());
        vo.setTitle(wish.getTitle());
        vo.setLocation(wish.getLocation());
        vo.setExpectedDate(wish.getExpectedDate() == null ? null : wish.getExpectedDate().toString());
        vo.setPriority(wish.getPriority());
        vo.setNote(wish.getNote());
        vo.setStatus(wish.getStatus());
        vo.setDoneDate(wish.getDoneDate() == null ? null : wish.getDoneDate().toString());
        vo.setJournalId(wish.getJournalId());
        vo.setJournalTitle(wish.getJournalId() == null ? null : journalTitles.get(wish.getJournalId()));
        vo.setCreateTime(wish.getCreateTime() == null ? null : wish.getCreateTime().toString());
        vo.setUpdateTime(wish.getUpdateTime() == null ? null : wish.getUpdateTime().toString());
        return vo;
    }

    public List<WishVO> toVOList(List<HikeWish> wishes, Map<Long, String> journalTitles) {
        return wishes.stream().map(w -> toVO(w, journalTitles)).toList();
    }
}
