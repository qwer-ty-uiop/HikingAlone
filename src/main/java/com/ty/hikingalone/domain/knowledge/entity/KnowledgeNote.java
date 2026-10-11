package com.ty.hikingalone.domain.knowledge.entity;

import com.ty.hikingalone.common.enums.knowledge.KnowledgeCategoryEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 知识笔记领域实体（聚合根）：长期有效的方法论，与「某一天的事」的日记相区分
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeNote {

    /** 标题长度上限 */
    public static final int MAX_TITLE_LENGTH = 100;

    /** 正文长度上限 */
    public static final int MAX_CONTENT_LENGTH = 10000;

    private Long id;

    private Long userId;

    private String title;

    /**
     * 分类（KnowledgeCategoryEnum.code）
     */
    private Integer category;

    /**
     * 正文（纯文本，前端按换行分段渲染）
     */
    private String content;

    /**
     * 0 普通 / 1 置顶
     */
    private Integer pinned;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：新建笔记（默认不置顶）
     */
    public static KnowledgeNote create(Long userId, String title, Integer category, String content) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        KnowledgeNote note = new KnowledgeNote();
        note.userId = userId;
        note.createTime = LocalDateTime.now();
        note.pinned = 0;
        note.applyContent(title, category, content);
        return note;
    }

    /**
     * 编辑笔记：非 null 字段覆盖（title/category/content/pinned）
     */
    public void update(String title, Integer category, String content, Integer pinned) {
        if (pinned != null && pinned != 0 && pinned != 1) {
            throw new IllegalArgumentException("置顶标记只能是 0 或 1");
        }
        applyContent(
                title == null ? this.title : title,
                category == null ? this.category : category,
                content == null ? this.content : content);
        if (pinned != null) {
            this.pinned = pinned;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 内容组装与校验
     */
    private void applyContent(String title, Integer category, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("标题不能超过 100 字");
        }
        KnowledgeCategoryEnum.fromCode(category);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("正文不能为空");
        }
        String c = content.trim();
        if (c.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("正文不能超过 10000 字");
        }
        this.title = t;
        this.category = category;
        this.content = c;
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static KnowledgeNote reconstruct(Long id, Long userId, String title, Integer category,
                                            String content, Integer pinned,
                                            LocalDateTime createTime, LocalDateTime updateTime) {
        KnowledgeNote note = new KnowledgeNote();
        note.id = id;
        note.userId = userId;
        note.title = title;
        note.category = category;
        note.content = content;
        note.pinned = pinned;
        note.createTime = createTime;
        note.updateTime = updateTime;
        return note;
    }
}
