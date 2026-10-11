package com.ty.hikingalone.infrastructure.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.knowledge.po.KnowledgeNotePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识笔记 Mapper
 */
@Mapper
public interface KnowledgeNoteMapper extends BaseMapper<KnowledgeNotePO> {
}
