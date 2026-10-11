package com.ty.hikingalone.application.gear;

import com.ty.hikingalone.application.gear.cmd.GearIdCmd;
import com.ty.hikingalone.application.gear.cmd.GearItemCmd;
import com.ty.hikingalone.domain.gear.entity.GearCatalogItem;
import com.ty.hikingalone.domain.gear.entity.GearItem;
import com.ty.hikingalone.domain.gear.repository.GearCatalogRepository;
import com.ty.hikingalone.domain.gear.repository.GearItemRepository;
import com.ty.hikingalone.interfaces.gear.converter.GearConverter;
import com.ty.hikingalone.interfaces.gear.vo.query.GearCatalogVO;
import com.ty.hikingalone.interfaces.gear.vo.query.GearItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 装备台账应用服务：CRUD 编排与归属校验（台账内容不变量在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GearItemService {

    private final GearItemRepository gearItemRepository;
    private final GearCatalogRepository gearCatalogRepository;
    private final GearConverter converter;

    /**
     * 全量装备列表
     */
    public List<GearItemVO> list(Long userId) {
        return converter.toItemVOList(gearItemRepository.listByUser(userId));
    }

    /**
     * 新建装备（默认在役）
     */
    @Transactional
    public GearItemVO create(Long userId, GearItemCmd cmd) {
        GearItem item = GearItem.create(userId, cmd.getName(), cmd.getCategory(),
                cmd.getWeightGrams(), cmd.getBrand(), cmd.getNote());
        item.attachId(gearItemRepository.save(item));
        return converter.toItemVO(item);
    }

    /**
     * 编辑装备（全量替换台账内容）
     */
    @Transactional
    public GearItemVO update(Long userId, GearItemCmd cmd) {
        GearItem existing = requireOwnedItem(userId, cmd.getId());
        existing.update(cmd.getName(), cmd.getCategory(), cmd.getWeightGrams(),
                cmd.getBrand(), cmd.getNote(), cmd.getStatus());
        gearItemRepository.save(existing);
        return converter.toItemVO(existing);
    }

    /**
     * 删除装备（物理删除；不级联清理清单条目——条目是快照）
     */
    @Transactional
    public void delete(Long userId, GearIdCmd cmd) {
        requireOwnedItem(userId, cmd.getId());
        gearItemRepository.delete(cmd.getId());
    }

    /** 目录全量上限（平台目录量级 ~数百条，联想前端本地过滤需要全量） */
    private static final int CATALOG_MAX_LIMIT = 500;

    /**
     * 常用装备目录：keyword 可空；limit 缺省/钳制 [1, 500]——前端开弹窗拉全量后本地过滤
     */
    public List<GearCatalogVO> searchCatalog(String keyword, Integer limit) {
        int l = limit == null ? CATALOG_MAX_LIMIT : Math.max(1, Math.min(CATALOG_MAX_LIMIT, limit));
        return gearCatalogRepository.search(keyword, l).stream()
                .map(GearCatalogVO::of)
                .toList();
    }

    /**
     * 归属校验：不存在或不属于当前用户统一报「装备不存在」
     */
    private GearItem requireOwnedItem(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("装备 id 不能为空");
        }
        GearItem item = gearItemRepository.findById(id);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new IllegalArgumentException("装备不存在");
        }
        return item;
    }
}
