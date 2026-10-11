package com.ty.hikingalone.interfaces.knowledge.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 笔记按 id 操作请求体
 */
@Data
public class KnowledgeIdDTO {

    /**
     * 笔记 id
     */
    @NotNull(message = "笔记 id 不能为空")
    private Long id;
}
