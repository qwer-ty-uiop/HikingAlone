package com.ty.hikingalone.domain.knowledge.repository;

import com.ty.hikingalone.domain.knowledge.entity.KnowledgeNote;

import java.util.List;

/**
 * 知识笔记仓储接口
 */
public interface KnowledgeNoteRepository {

    /**
     * 查某用户全部笔记（置顶在前，其余按更新时间倒序）
     */
    List<KnowledgeNote> listByUser(Long userId);

    /**
     * 按主键查笔记
     */
    KnowledgeNote findById(Long id);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(KnowledgeNote note);

    /**
     * 按主键物理删除
     */
    void delete(Long id);
}
