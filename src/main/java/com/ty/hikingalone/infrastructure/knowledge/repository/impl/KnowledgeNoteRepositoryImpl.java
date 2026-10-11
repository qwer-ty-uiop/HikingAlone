package com.ty.hikingalone.infrastructure.knowledge.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ty.hikingalone.domain.knowledge.entity.KnowledgeNote;
import com.ty.hikingalone.domain.knowledge.repository.KnowledgeNoteRepository;
import com.ty.hikingalone.infrastructure.knowledge.mapper.KnowledgeNoteMapper;
import com.ty.hikingalone.infrastructure.knowledge.po.KnowledgeNotePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识笔记仓储实现
 */
@Repository
@RequiredArgsConstructor
public class KnowledgeNoteRepositoryImpl implements KnowledgeNoteRepository {

    private final KnowledgeNoteMapper noteMapper;

    @Override
    public List<KnowledgeNote> listByUser(Long userId) {
        return noteMapper.selectList(
                new LambdaQueryWrapper<KnowledgeNotePO>()
                        .eq(KnowledgeNotePO::getUserId, userId)
                        .orderByDesc(KnowledgeNotePO::getPinned)
                        .orderByDesc(KnowledgeNotePO::getUpdateTime)
                        .orderByDesc(KnowledgeNotePO::getId)
        ).stream().map(this::toEntity).toList();
    }

    @Override
    public KnowledgeNote findById(Long id) {
        KnowledgeNotePO po = noteMapper.selectById(id);
        return po == null ? null : toEntity(po);
    }

    @Override
    public Long save(KnowledgeNote note) {
        KnowledgeNotePO po = new KnowledgeNotePO();
        po.setUserId(note.getUserId());
        po.setTitle(note.getTitle());
        po.setCategory(note.getCategory());
        po.setContent(note.getContent());
        po.setPinned(note.getPinned());
        po.setCreateTime(note.getCreateTime());
        po.setUpdateTime(note.getUpdateTime());
        if (note.getId() != null) {
            po.setId(note.getId());
            noteMapper.updateById(po);
            return note.getId();
        }
        noteMapper.insert(po);
        return po.getId();
    }

    @Override
    public void delete(Long id) {
        noteMapper.deleteById(id);
    }

    private KnowledgeNote toEntity(KnowledgeNotePO po) {
        return KnowledgeNote.reconstruct(
                po.getId(), po.getUserId(), po.getTitle(), po.getCategory(),
                po.getContent(), po.getPinned(), po.getCreateTime(), po.getUpdateTime());
    }
}
