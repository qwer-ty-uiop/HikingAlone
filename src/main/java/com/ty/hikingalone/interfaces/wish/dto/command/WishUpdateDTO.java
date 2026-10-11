package com.ty.hikingalone.interfaces.wish.dto.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编辑心愿请求体：非 null 字段覆盖；status 仅允许 0↔1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WishUpdateDTO extends WishSaveDTO {

    /**
     * 心愿 id
     */
    @NotNull(message = "心愿 id 不能为空")
    private Long id;

    /**
     * 状态 0 想去 / 1 已计划（可空=不更新；完成/放弃走专用接口）
     */
    private Integer status;
}
