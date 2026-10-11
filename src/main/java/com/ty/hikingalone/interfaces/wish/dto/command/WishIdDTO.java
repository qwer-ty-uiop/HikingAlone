package com.ty.hikingalone.interfaces.wish.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 心愿按 id 操作请求体（放弃/删除）
 */
@Data
public class WishIdDTO {

    /**
     * 心愿 id
     */
    @NotNull(message = "心愿 id 不能为空")
    private Long id;
}
