package com.ty.hikingalone.interfaces.journal.vo.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 徒步日记详情 VO：完整正文
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JournalDetailVO extends JournalVO {

    /** 完整正文（可空） */
    private String content;

    /** 创建时间（yyyy-MM-dd'T'HH:mm:ss） */
    private String createTime;

    /** 最近修改时间（未修改过为 null） */
    private String updateTime;
}
