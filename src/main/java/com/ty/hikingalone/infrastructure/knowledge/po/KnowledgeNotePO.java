package com.ty.hikingalone.infrastructure.knowledge.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识笔记表 PO
 */
@Data
@TableName("knowledge_note")
public class KnowledgeNotePO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /**
     * 分类 1~7
     */
    private Integer category;

    /**
     * 正文
     */
    private String content;

    /**
     * 0 普通 / 1 置顶
     */
    private Integer pinned;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
