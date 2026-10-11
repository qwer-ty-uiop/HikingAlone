package com.ty.hikingalone.application.gear.cmd;

import lombok.Data;

/**
 * 装备/清单/条目按 id 操作命令（删除、复制等）
 */
@Data
public class GearIdCmd {

    /** 目标 id */
    private Long id;
}
