package com.ty.hikingalone.application.gear;

import com.ty.hikingalone.application.gear.cmd.ChecklistItemAddCmd;
import com.ty.hikingalone.application.gear.cmd.ChecklistItemUpdateCmd;
import com.ty.hikingalone.application.gear.cmd.GearChecklistCmd;
import com.ty.hikingalone.application.gear.cmd.GearIdCmd;
import com.ty.hikingalone.domain.gear.entity.GearChecklist;
import com.ty.hikingalone.domain.gear.entity.GearChecklistItem;
import com.ty.hikingalone.domain.gear.entity.GearChecklistItem;
import com.ty.hikingalone.domain.gear.repository.GearChecklistRepository;
import com.ty.hikingalone.interfaces.gear.converter.GearConverter;
import com.ty.hikingalone.interfaces.gear.vo.query.GearChecklistVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 打包清单应用服务：归属链校验（条目操作先查清单属主）、复制清单、级联删除
 * （条目内容/数量上限等不变量在清单聚合内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GearChecklistService {

    /** 复制清单的标题后缀 */
    private static final String COPY_TITLE_SUFFIX = " · 副本";

    private final GearChecklistRepository checklistRepository;
    private final GearConverter converter;

    /**
     * 全量清单（含条目）
     */
    public List<GearChecklistVO> list(Long userId) {
        return converter.toChecklistVOList(checklistRepository.listByUser(userId));
    }

    /**
     * 新建空清单
     */
    @Transactional
    public GearChecklistVO create(Long userId, GearChecklistCmd cmd) {
        GearChecklist checklist = GearChecklist.create(userId, cmd.getTitle(),
                parseDate(cmd.getHikeDate()), cmd.getNote());
        checklist.attachId(checklistRepository.saveHead(checklist));
        return converter.toChecklistVO(checklist);
    }

    /**
     * 编辑清单头（非 null 覆盖）
     */
    @Transactional
    public GearChecklistVO update(Long userId, GearChecklistCmd cmd) {
        GearChecklist existing = requireOwnedChecklist(userId, cmd.getId());
        existing.updateHead(cmd.getTitle(), parseDate(cmd.getHikeDate()), cmd.getNote(), cmd.getStatus());
        checklistRepository.saveHead(existing);
        return converter.toChecklistVO(existing);
    }

    /**
     * 删除清单（级联删条目）
     */
    @Transactional
    public void delete(Long userId, GearIdCmd cmd) {
        requireOwnedChecklist(userId, cmd.getId());
        checklistRepository.deleteHeadCascade(cmd.getId());
    }

    /**
     * 复制清单：标题追加「 · 副本」，状态重置准备中，条目全带但勾选清零
     */
    @Transactional
    public GearChecklistVO copy(Long userId, GearIdCmd cmd) {
        GearChecklist source = requireOwnedChecklist(userId, cmd.getId());
        GearChecklist copy = GearChecklist.create(userId, source.getTitle() + COPY_TITLE_SUFFIX,
                source.getHikeDate(), source.getNote());
        copy.attachId(checklistRepository.saveHead(copy));
        for (GearChecklistItem item : source.getItems()) {
            GearChecklistItem copied = copy.addItem(item.getGearItemId(), item.getName(), item.getWeightGrams());
            checklistRepository.saveItem(copied);
        }
        return converter.toChecklistVO(copy);
    }

    /**
     * 加条目（走聚合校验条目数上限与 sort 分配）
     */
    @Transactional
    public GearChecklistVO addItem(Long userId, ChecklistItemAddCmd cmd) {
        GearChecklist checklist = requireOwnedChecklist(userId, cmd.getChecklistId());
        GearChecklistItem item = checklist.addItem(cmd.getGearItemId(), cmd.getName(), cmd.getWeightGrams());
        checklistRepository.saveItem(item);
        return converter.toChecklistVO(checklist);
    }

    /**
     * 更新条目（勾选/改名/改克重/换仓位，非 null 覆盖）
     */
    @Transactional
    public GearChecklistVO updateItem(Long userId, ChecklistItemUpdateCmd cmd) {
        log.info("updateItem in: id={} checked={} name={} weight={} slot={}", cmd.getId(), cmd.getChecked(), cmd.getName(), cmd.getWeightGrams(), cmd.getSlot());
        Long checklistId = findChecklistIdByItem(userId, cmd.getId());
        GearChecklist checklist = requireOwnedChecklist(userId, checklistId);
        checklist.updateItem(cmd.getId(), cmd.getChecked(), cmd.getName(), cmd.getWeightGrams(), cmd.getSlot());
        log.info("updateItem merged: slot={}", checklist.getItems().stream().filter(i -> i.getId().equals(cmd.getId())).findFirst().map(GearChecklistItem::getSlot).orElse(null));
        checklist.getItems().stream()
                .filter(i -> i.getId().equals(cmd.getId()))
                .findFirst()
                .ifPresent(checklistRepository::saveItem);
        return converter.toChecklistVO(checklist);
    }

    /**
     * 删条目
     */
    @Transactional
    public GearChecklistVO deleteItem(Long userId, GearIdCmd cmd) {
        GearChecklist checklist = requireOwnedChecklist(userId, findChecklistIdByItem(userId, cmd.getId()));
        checklist.removeItem(cmd.getId());
        checklistRepository.deleteItem(cmd.getId());
        return converter.toChecklistVO(checklist);
    }

    /**
     * 清单归属校验：不存在或不属于当前用户统一报「清单不存在」
     */
    private GearChecklist requireOwnedChecklist(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("清单 id 不能为空");
        }
        GearChecklist checklist = checklistRepository.findById(id);
        if (checklist == null || !userId.equals(checklist.getUserId())) {
            throw new IllegalArgumentException("清单不存在");
        }
        return checklist;
    }

    /**
     * 条目反查所属清单并顺带校验归属（条目 id 无法直接定位清单属主，先反查再校验）
     */
    private Long findChecklistIdByItem(Long userId, Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("条目 id 不能为空");
        }
        for (GearChecklist checklist : checklistRepository.listByUser(userId)) {
            if (checklist.getItems().stream().anyMatch(i -> i.getId().equals(itemId))) {
                return checklist.getId();
            }
        }
        throw new IllegalArgumentException("清单条目不存在");
    }

    /**
     * 解析出行日期：空=不更新；格式错误转 400
     */
    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("出行日期格式错误，应为 yyyy-MM-dd");
        }
    }
}
