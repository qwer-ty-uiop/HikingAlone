package com.ty.hikingalone.application.gear.cmd;

import lombok.Data;

/**
 * 清单加条目命令
 */
@Data
public class ChecklistItemAddCmd {

    /** 所属清单 id */
    private Long checklistId;

    /** 来源装备 id（可空，仅标记来源） */
    private Long gearItemId;

    /** 条目名（1~100 字） */
    private String name;

    /** 克重快照（可空，[0,30000]） */
    private Integer weightGrams;
}
