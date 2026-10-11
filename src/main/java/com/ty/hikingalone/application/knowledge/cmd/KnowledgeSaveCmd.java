package com.ty.hikingalone.application.knowledge.cmd;

import lombok.Data;

/**
 * 新建/编辑笔记命令（编辑时 id 必填；字段非空才覆盖）
 */
@Data
public class KnowledgeSaveCmd {

    /** 笔记 id（编辑必填） */
    private Long id;

    /** 标题（1~100 字） */
    private String title;

    /** 分类（1~7） */
    private Integer category;

    /** 正文（1~10000 字） */
    private String content;

    /** 置顶 0/1（可空=不更新） */
    private Integer pinned;
}
