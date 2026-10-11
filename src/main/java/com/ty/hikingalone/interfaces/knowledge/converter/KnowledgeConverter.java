package com.ty.hikingalone.interfaces.knowledge.converter;

import com.ty.hikingalone.application.knowledge.cmd.KnowledgeIdCmd;
import com.ty.hikingalone.application.knowledge.cmd.KnowledgeSaveCmd;
import com.ty.hikingalone.common.enums.knowledge.KnowledgeCategoryEnum;
import com.ty.hikingalone.domain.knowledge.entity.KnowledgeNote;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeIdDTO;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeSaveDTO;
import com.ty.hikingalone.interfaces.knowledge.dto.command.KnowledgeUpdateDTO;
import com.ty.hikingalone.interfaces.knowledge.vo.query.KnowledgeNoteVO;
import org.springframework.stereotype.Component;

/**
 * 知识笔记 领域实体/请求体 → VO 转换器
 */
@Component
public class KnowledgeConverter {

    public KnowledgeSaveCmd toSaveCmd(KnowledgeSaveDTO dto) {
        KnowledgeSaveCmd cmd = new KnowledgeSaveCmd();
        cmd.setTitle(dto.getTitle());
        cmd.setCategory(dto.getCategory());
        cmd.setContent(dto.getContent());
        return cmd;
    }

    public KnowledgeSaveCmd toSaveCmd(KnowledgeUpdateDTO dto) {
        KnowledgeSaveCmd cmd = new KnowledgeSaveCmd();
        cmd.setId(dto.getId());
        cmd.setTitle(dto.getTitle());
        cmd.setCategory(dto.getCategory());
        cmd.setContent(dto.getContent());
        cmd.setPinned(dto.getPinned());
        return cmd;
    }

    public KnowledgeIdCmd toIdCmd(KnowledgeIdDTO dto) {
        KnowledgeIdCmd cmd = new KnowledgeIdCmd();
        cmd.setId(dto.getId());
        return cmd;
    }

    public KnowledgeNoteVO toVO(KnowledgeNote note) {
        KnowledgeNoteVO vo = new KnowledgeNoteVO();
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setCategory(note.getCategory());
        vo.setCategoryLabel(KnowledgeCategoryEnum.fromCode(note.getCategory()).getDesc());
        vo.setContent(note.getContent());
        vo.setPinned(note.getPinned());
        vo.setCreateTime(note.getCreateTime() == null ? null : note.getCreateTime().toString());
        vo.setUpdateTime(note.getUpdateTime() == null ? null : note.getUpdateTime().toString());
        return vo;
    }
}
