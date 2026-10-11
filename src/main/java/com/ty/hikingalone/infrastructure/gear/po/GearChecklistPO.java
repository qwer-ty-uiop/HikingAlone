package com.ty.hikingalone.infrastructure.gear.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打包清单头表 PO
 */
@Data
@TableName("gear_checklist")
public class GearChecklistPO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /**
     * 对应出行日期（可空）
     */
    private LocalDate hikeDate;

    /**
     * 备注（可空）
     */
    private String note;

    /**
     * 0 准备中 / 1 已打包 / 2 已结束
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
