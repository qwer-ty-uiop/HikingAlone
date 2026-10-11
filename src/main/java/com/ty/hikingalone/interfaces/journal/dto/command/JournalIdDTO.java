package com.ty.hikingalone.interfaces.journal.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除徒步日记请求体
 */
@Data
public class JournalIdDTO {

    /**
     * 日记 id
     */
    @NotNull(message = "日记 id 不能为空")
    private Long id;
}
