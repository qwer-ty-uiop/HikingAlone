package com.ty.hikingalone.interfaces.daily.vo.query;

import lombok.Data;

import java.util.List;

/**
 * GET /daily/recent 返回体：区间内升序记录 + 汇总统计
 */
@Data
public class DailyRecentVO {

    /** 升序记录（供折线图/列表直接使用） */
    private List<DailyCheckinVO> records;

    /** 汇总统计 */
    private DailySummaryVO summary;

    /** 本次查询的天数窗口 */
    private Integer days;
}
