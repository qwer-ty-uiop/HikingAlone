package com.ty.hikingalone.interfaces.gear.vo.query;

import lombok.Data;

import java.util.List;

/**
 * 打包清单 VO（含全部条目，按 sort 升序）
 */
@Data
public class GearChecklistVO {

    private Long id;

    /** 清单标题 */
    private String title;

    /** 对应出行日期 yyyy-MM-dd（可空） */
    private String hikeDate;

    /** 备注（可空） */
    private String note;

    /** 0 准备中 / 1 已打包 / 2 已结束 */
    private Integer status;

    /** 创建时间（yyyy-MM-dd'T'HH:mm:ss） */
    private String createTime;

    /** 全部条目 */
    private List<GearChecklistItemVO> items;
}
