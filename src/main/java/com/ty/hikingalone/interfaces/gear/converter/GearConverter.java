package com.ty.hikingalone.interfaces.gear.converter;

import com.ty.hikingalone.application.gear.cmd.ChecklistItemAddCmd;
import com.ty.hikingalone.application.gear.cmd.ChecklistItemUpdateCmd;
import com.ty.hikingalone.application.gear.cmd.GearChecklistCmd;
import com.ty.hikingalone.application.gear.cmd.GearIdCmd;
import com.ty.hikingalone.application.gear.cmd.GearItemCmd;
import com.ty.hikingalone.domain.gear.entity.GearChecklist;
import com.ty.hikingalone.domain.gear.entity.GearChecklistItem;
import com.ty.hikingalone.domain.gear.entity.GearItem;
import com.ty.hikingalone.interfaces.gear.dto.command.ChecklistItemAddDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.ChecklistItemUpdateDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearChecklistSaveDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearChecklistUpdateDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearIdDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearItemSaveDTO;
import com.ty.hikingalone.interfaces.gear.dto.command.GearItemUpdateDTO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearChecklistItemVO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearChecklistVO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearItemVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 装备模块 领域实体/请求体 → VO 转换器
 */
@Component
public class GearConverter {

    // ===== 请求体 → 命令 =====

    public GearItemCmd toItemCmd(GearItemSaveDTO dto) {
        GearItemCmd cmd = new GearItemCmd();
        cmd.setName(dto.getName());
        cmd.setCategory(dto.getCategory());
        cmd.setWeightGrams(dto.getWeightGrams());
        cmd.setBrand(dto.getBrand());
        cmd.setNote(dto.getNote());
        return cmd;
    }

    public GearItemCmd toItemCmd(GearItemUpdateDTO dto) {
        GearItemCmd cmd = toItemCmd((GearItemSaveDTO) dto);
        cmd.setId(dto.getId());
        cmd.setStatus(dto.getStatus());
        return cmd;
    }

    public GearIdCmd toIdCmd(GearIdDTO dto) {
        GearIdCmd cmd = new GearIdCmd();
        cmd.setId(dto.getId());
        return cmd;
    }

    public GearChecklistCmd toChecklistCmd(GearChecklistSaveDTO dto) {
        GearChecklistCmd cmd = new GearChecklistCmd();
        cmd.setTitle(dto.getTitle());
        cmd.setHikeDate(dto.getHikeDate());
        cmd.setNote(dto.getNote());
        return cmd;
    }

    public GearChecklistCmd toChecklistCmd(GearChecklistUpdateDTO dto) {
        GearChecklistCmd cmd = new GearChecklistCmd();
        cmd.setId(dto.getId());
        cmd.setTitle(dto.getTitle());
        cmd.setHikeDate(dto.getHikeDate());
        cmd.setNote(dto.getNote());
        cmd.setStatus(dto.getStatus());
        return cmd;
    }

    public ChecklistItemAddCmd toItemAddCmd(ChecklistItemAddDTO dto) {
        ChecklistItemAddCmd cmd = new ChecklistItemAddCmd();
        cmd.setChecklistId(dto.getChecklistId());
        cmd.setGearItemId(dto.getGearItemId());
        cmd.setName(dto.getName());
        cmd.setWeightGrams(dto.getWeightGrams());
        return cmd;
    }

    public ChecklistItemUpdateCmd toItemUpdateCmd(ChecklistItemUpdateDTO dto) {
        ChecklistItemUpdateCmd cmd = new ChecklistItemUpdateCmd();
        cmd.setId(dto.getId());
        cmd.setChecked(dto.getChecked());
        cmd.setName(dto.getName());
        cmd.setWeightGrams(dto.getWeightGrams());
        return cmd;
    }

    // ===== 领域实体 → VO =====

    public GearItemVO toItemVO(GearItem item) {
        GearItemVO vo = new GearItemVO();
        vo.setId(item.getId());
        vo.setName(item.getName());
        vo.setCategory(item.getCategory());
        vo.setWeightGrams(item.getWeightGrams());
        vo.setBrand(item.getBrand());
        vo.setNote(item.getNote());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime() == null ? null : item.getCreateTime().toString());
        return vo;
    }

    public List<GearItemVO> toItemVOList(List<GearItem> items) {
        return items.stream().map(this::toItemVO).toList();
    }

    public GearChecklistVO toChecklistVO(GearChecklist checklist) {
        GearChecklistVO vo = new GearChecklistVO();
        vo.setId(checklist.getId());
        vo.setTitle(checklist.getTitle());
        vo.setHikeDate(checklist.getHikeDate() == null ? null : checklist.getHikeDate().toString());
        vo.setNote(checklist.getNote());
        vo.setStatus(checklist.getStatus());
        vo.setCreateTime(checklist.getCreateTime() == null ? null : checklist.getCreateTime().toString());
        vo.setItems(toChecklistItemVOList(checklist.getItems()));
        return vo;
    }

    public List<GearChecklistVO> toChecklistVOList(List<GearChecklist> checklists) {
        return checklists.stream().map(this::toChecklistVO).toList();
    }

    public List<GearChecklistItemVO> toChecklistItemVOList(List<GearChecklistItem> items) {
        return items.stream().map(item -> {
            GearChecklistItemVO vo = new GearChecklistItemVO();
            vo.setId(item.getId());
            vo.setGearItemId(item.getGearItemId());
            vo.setName(item.getName());
            vo.setWeightGrams(item.getWeightGrams());
            vo.setChecked(item.getChecked());
            vo.setSort(item.getSort());
            return vo;
        }).toList();
    }
}
