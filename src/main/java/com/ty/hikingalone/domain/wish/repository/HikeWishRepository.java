package com.ty.hikingalone.domain.wish.repository;

import com.ty.hikingalone.domain.wish.entity.HikeWish;

import java.util.List;

/**
 * 徒步心愿仓储接口
 */
public interface HikeWishRepository {

    /**
     * 查某用户全部心愿（状态升序 + 优先级降序 + 期望日期升序）
     */
    List<HikeWish> listByUser(Long userId);

    /**
     * 按主键查心愿
     */
    HikeWish findById(Long id);

    /**
     * 保存：id 为空 insert（返回生成主键），否则 update（返回原 id）
     */
    Long save(HikeWish wish);

    /**
     * 按主键物理删除
     */
    void delete(Long id);
}
