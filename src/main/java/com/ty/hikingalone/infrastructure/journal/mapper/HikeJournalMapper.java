package com.ty.hikingalone.infrastructure.journal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ty.hikingalone.infrastructure.journal.po.HikeJournalPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 徒步日记 Mapper
 */
@Mapper
public interface HikeJournalMapper extends BaseMapper<HikeJournalPO> {
}
