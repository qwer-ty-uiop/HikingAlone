package com.ty.hikingalone.interfaces.achievement.vo.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单枚徽章 VO
 */
@Data
public class AchievementVO {

    /** 徽章编码 */
    private String code;

    /** 徽章名 */
    private String name;

    /** 达成条件描述 */
    private String description;

    /** 是否已达成 */
    private Boolean achieved;

    /** 当前口径值 */
    private BigDecimal current;

    /** 阈值 */
    private BigDecimal target;

    /** 进度百分比（min(100, floor(current/target*100))） */
    private Integer progressPercent;
}
