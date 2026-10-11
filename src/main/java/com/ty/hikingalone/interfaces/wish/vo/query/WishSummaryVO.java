package com.ty.hikingalone.interfaces.wish.vo.query;

import lombok.Data;

/**
 * 心愿汇总 VO
 */
@Data
public class WishSummaryVO {

    /** 想去数量 */
    private Integer wishCount = 0;

    /** 已计划数量 */
    private Integer plannedCount = 0;

    /** 已完成数量 */
    private Integer doneCount = 0;

    /** 已放弃数量 */
    private Integer givenUpCount = 0;
}
