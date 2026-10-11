package com.ty.hikingalone.application.gear.cmd;

import lombok.Data;

/**
 * 清单条目更新命令：非 null 字段覆盖（勾选/改名/改克重）
 */
@Data
public class ChecklistItemUpdateCmd {

    /** 条目 id */
    private Long id;

    /** 打包状态 0/1（可空=不更新） */
    private Integer checked;

    /** 条目名（可空=不更新） */
    private String name;

    /** 克重快照（可空=不更新） */
    private Integer weightGrams;

    /** 装载仓位 0~4（可空=不更新；装包拼图自由换位） */
    private Integer slot;
}
