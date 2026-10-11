package com.ty.hikingalone.interfaces.knowledge.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑笔记请求体：非 null 字段覆盖（可置顶）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeUpdateDTO extends KnowledgeSaveDTO {

    /**
     * 笔记 id
     */
    @NotNull(message = "笔记 id 不能为空")
    private Long id;

    /**
     * 置顶 0/1（可空=不更新）
     */
    private Integer pinned;
}
