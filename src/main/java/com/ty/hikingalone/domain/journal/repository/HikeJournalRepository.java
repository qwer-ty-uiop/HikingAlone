package com.ty.hikingalone.domain.journal.repository;

import com.ty.hikingalone.domain.journal.entity.HikeJournal;

import java.util.List;

/**
 * 徒步日记仓储接口（领域层定义语义，实现收敛在基础设施层）
 */
public interface HikeJournalRepository {

    /**
     * 查某用户的全部日记，按徒步日期倒序（汇总/目标/成就/统计的聚合数据源）
     */
    List<HikeJournal> listByUser(Long userId);

    /**
     * 查某用户某年的日记（按徒步日期倒序）
     */
    List<HikeJournal> listByUserAndYear(Long userId, int year);

    /**
     * 查某用户最近 limit 条日记（按徒步日期倒序、同日按 id 倒序）
     */
    List<HikeJournal> listRecent(Long userId, int limit);

    /**
     * 按主键查日记（详情与归属校验）
     */
    HikeJournal findById(Long id);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(HikeJournal journal);

    /**
     * 按主键物理删除
     */
    void delete(Long id);
}
