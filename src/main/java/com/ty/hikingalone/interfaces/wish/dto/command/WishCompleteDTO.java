package com.ty.hikingalone.interfaces.wish.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 心愿标记完成请求体
 */
@Data
public class WishCompleteDTO {

    /**
     * 心愿 id
     */
    @NotNull(message = "心愿 id 不能为空")
    private Long id;

    /**
     * 完成日期（yyyy-MM-dd，可空=今天）
     */
    private String doneDate;

    /**
     * 关联日记 id（可空；非空时校验归属）
     */
    private Long journalId;
}
