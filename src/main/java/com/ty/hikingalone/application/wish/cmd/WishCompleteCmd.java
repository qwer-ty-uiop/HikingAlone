package com.ty.hikingalone.application.wish.cmd;

import lombok.Data;

/**
 * 心愿标记完成命令
 */
@Data
public class WishCompleteCmd {

    /** 心愿 id */
    private Long id;

    /** 完成日期（yyyy-MM-dd，可空=今天） */
    private String doneDate;

    /** 关联日记 id（可空；非空时校验归属） */
    private Long journalId;
}
