package com.ty.hikingalone.application.gear.cmd;

import lombok.Data;

/**
 * 新建/编辑打包清单命令（编辑时 id 必填；title/hikeDate/note/status 非空才覆盖）
 * <p>hikeDate 为 yyyy-MM-dd 文本，由应用服务解析</p>
 */
@Data
public class GearChecklistCmd {

    /** 清单 id（编辑必填） */
    private Long id;

    /** 清单标题（1~100 字） */
    private String title;

    /** 对应出行日期（yyyy-MM-dd，可空=不更新） */
    private String hikeDate;

    /** 备注（可空，≤200 字） */
    private String note;

    /** 状态（GearChecklistStatusEnum.code，可空=不更新） */
    private Integer status;
}
