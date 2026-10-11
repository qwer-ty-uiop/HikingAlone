package com.ty.hikingalone.interfaces.knowledge.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新建笔记请求体
 */
@Data
public class KnowledgeSaveDTO {

    /**
     * 标题（1~100 字）
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 分类（1~7）
     */
    @NotNull(message = "分类不能为空")
    private Integer category;

    /**
     * 正文（1~10000 字）
     */
    @NotBlank(message = "正文不能为空")
    private String content;
}
