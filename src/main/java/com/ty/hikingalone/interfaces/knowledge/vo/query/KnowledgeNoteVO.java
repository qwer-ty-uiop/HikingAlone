package com.ty.hikingalone.interfaces.knowledge.vo.query;

import lombok.Data;

/**
 * 知识笔记 VO（含全量 content，列表直读、详情免请求）
 */
@Data
public class KnowledgeNoteVO {

    private Long id;

    /** 标题 */
    private String title;

    /** 分类 1~7 */
    private Integer category;

    /** 分类文案 */
    private String categoryLabel;

    /** 正文 */
    private String content;

    /** 0 普通 / 1 置顶 */
    private Integer pinned;

    /** 创建时间（yyyy-MM-dd'T'HH:mm:ss） */
    private String createTime;

    /** 最近修改时间（未修改过为 null） */
    private String updateTime;
}
