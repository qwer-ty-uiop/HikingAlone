package com.ty.hikingalone.application.wish.cmd;

import lombok.Data;

/**
 * 心愿按 id 操作命令（放弃/删除）
 */
@Data
public class WishIdCmd {

    /** 心愿 id */
    private Long id;
}
