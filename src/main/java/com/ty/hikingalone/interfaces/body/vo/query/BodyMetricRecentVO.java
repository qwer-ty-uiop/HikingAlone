package com.ty.hikingalone.interfaces.body.vo.query;

import lombok.Data;

import java.util.List;

/**
 * GET /body/recent 返回体：区间内升序记录 + 汇总统计
 */
@Data
public class BodyMetricRecentVO {

    /** 升序记录（供折线图/列表直接使用） */
    private List<BodyMetricRecordVO> records;

    /** 汇总统计 */
    private BodyMetricSummaryVO summary;

    /** 本次查询的天数窗口 */
    private Integer days;
}
