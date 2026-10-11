package com.ty.hikingalone.interfaces.journal.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑徒步日记请求体：在保存字段之上要求 id
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JournalUpdateDTO extends JournalSaveDTO {

    /**
     * 日记 id（编辑必填）
     */
    @NotNull(message = "日记 id 不能为空")
    private Long id;
}
