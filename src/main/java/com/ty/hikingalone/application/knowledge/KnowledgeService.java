package com.ty.hikingalone.application.knowledge;

import com.ty.hikingalone.application.knowledge.cmd.KnowledgeIdCmd;
import com.ty.hikingalone.application.knowledge.cmd.KnowledgeSaveCmd;
import com.ty.hikingalone.domain.knowledge.entity.KnowledgeNote;
import com.ty.hikingalone.domain.knowledge.repository.KnowledgeNoteRepository;
import com.ty.hikingalone.interfaces.knowledge.converter.KnowledgeConverter;
import com.ty.hikingalone.interfaces.knowledge.vo.query.KnowledgeNoteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库应用服务：CRUD 编排、关键词内存过滤（个人量级无需全文索引）
 * （内容不变量在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeNoteRepository noteRepository;
    private final KnowledgeConverter converter;

    /**
     * 新建笔记（默认不置顶）
     */
    @Transactional
    public KnowledgeNoteVO create(Long userId, KnowledgeSaveCmd cmd) {
        KnowledgeNote note = KnowledgeNote.create(userId, cmd.getTitle(), cmd.getCategory(), cmd.getContent());
        note.attachId(noteRepository.save(note));
        return converter.toVO(note);
    }

    /**
     * 编辑笔记（非 null 覆盖，可置顶）
     */
    @Transactional
    public KnowledgeNoteVO update(Long userId, KnowledgeSaveCmd cmd) {
        KnowledgeNote existing = requireOwnedNote(userId, cmd.getId());
        existing.update(cmd.getTitle(), cmd.getCategory(), cmd.getContent(), cmd.getPinned());
        noteRepository.save(existing);
        return converter.toVO(existing);
    }

    /**
     * 删除笔记
     */
    @Transactional
    public void delete(Long userId, KnowledgeIdCmd cmd) {
        requireOwnedNote(userId, cmd.getId());
        noteRepository.delete(cmd.getId());
    }

    /**
     * 笔记列表：关键词对 title+content 内存过滤、分类过滤；置顶在前，其余按更新时间倒序
     */
    public List<KnowledgeNoteVO> list(Long userId, String keyword, Integer category) {
        List<KnowledgeNote> notes = noteRepository.listByUser(userId);
        return notes.stream()
                .filter(n -> category == null || category.equals(n.getCategory()))
                .filter(n -> matchesKeyword(n, keyword))
                .map(converter::toVO)
                .toList();
    }

    /**
     * 归属校验：不存在或不属于当前用户统一报「笔记不存在」
     */
    private KnowledgeNote requireOwnedNote(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("笔记 id 不能为空");
        }
        KnowledgeNote note = noteRepository.findById(id);
        if (note == null || !userId.equals(note.getUserId())) {
            throw new IllegalArgumentException("笔记不存在");
        }
        return note;
    }

    /**
     * 关键词匹配：空关键词全通过；title 或 content 包含即命中
     */
    private boolean matchesKeyword(KnowledgeNote note, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.trim();
        return (note.getTitle() != null && note.getTitle().contains(k))
                || (note.getContent() != null && note.getContent().contains(k));
    }
}
