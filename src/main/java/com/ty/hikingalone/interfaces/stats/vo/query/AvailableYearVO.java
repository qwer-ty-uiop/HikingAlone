package com.ty.hikingalone.interfaces.stats.vo.query;

import lombok.Data;

/**
 * 可选年份 VO（年份切换器数据源）
 */
@Data
public class AvailableYearVO {

    /** 年份 */
    private Integer year;

    /** 该年徒步次数 */
    private Integer hikeCount;
}
