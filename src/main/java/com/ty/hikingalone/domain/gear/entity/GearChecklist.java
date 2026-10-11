package com.ty.hikingalone.domain.gear.entity;

import com.ty.hikingalone.common.enums.gear.GearChecklistStatusEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 打包清单领域实体（聚合根）：持有全部条目，条目的添加/更新/移除与 sort 连续性由聚合维护。
 * <p>写入通过仓储的细粒度方法落库（saveHead / saveItem / deleteItem），由应用服务编排事务</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GearChecklist {

    /** 标题长度上限 */
    public static final int MAX_TITLE_LENGTH = 100;

    /** 备注长度上限 */
    public static final int MAX_NOTE_LENGTH = 200;

    private Long id;

    private Long userId;

    private String title;

    /**
     * 对应出行日期（可空）
     */
    private LocalDate hikeDate;

    /**
     * 备注（可空，trim 后落库）
     */
    private String note;

    /**
     * 状态（GearChecklistStatusEnum.code：0 准备中 / 1 已打包 / 2 已结束）
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 全部条目（按 sort 升序）；仅读场景与聚合内校验使用，写入走仓储细粒度方法
     */
    private List<GearChecklistItem> items = new ArrayList<>();

    /**
     * 工厂方法：新建空清单，默认准备中
     */
    public static GearChecklist create(Long userId, String title, LocalDate hikeDate, String note) {
        GearChecklist checklist = new GearChecklist();
        checklist.userId = userId;
        checklist.createTime = LocalDateTime.now();
        checklist.applyContent(title, hikeDate, note);
        checklist.status = GearChecklistStatusEnum.PREPARING.getCode();
        return checklist;
    }

    /**
     * 编辑清单头：非 null 字段覆盖（title/hikeDate/note/status 任一可空表示不更新）
     */
    public void updateHead(String title, LocalDate hikeDate, String note, Integer status) {
        if (title != null || hikeDate != null || note != null) {
            applyContent(title == null ? this.title : title,
                    hikeDate == null ? this.hikeDate : hikeDate,
                    note == null ? this.note : note);
        }
        if (status != null) {
            GearChecklistStatusEnum.fromCode(status);
            this.status = status;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 添加条目：校验条目数上限并分配 sort（当前最大值 + 1）
     */
    public GearChecklistItem addItem(Long gearItemId, String name, Integer weightGrams) {
        if (items.size() >= GearChecklistItem.MAX_ITEMS_PER_CHECKLIST) {
            throw new IllegalArgumentException("清单条目已达上限（100）");
        }
        int nextSort = items.stream()
                .mapToInt(GearChecklistItem::getSort)
                .max()
                .orElse(-1) + 1;
        GearChecklistItem item = GearChecklistItem.create(this.id, gearItemId, name, weightGrams, nextSort);
        this.items.add(item);
        return item;
    }

    /**
     * 更新条目（部分覆盖：勾选/改名/改克重/换仓位）；条目必须属于本清单
     */
    public void updateItem(Long itemId, Integer checked, String name, Integer weightGrams, Integer slot) {
        GearChecklistItem item = requireItem(itemId);
        item.merge(checked, name, weightGrams, slot);
    }

    /**
     * 从聚合内移除条目（排序空洞不重排，展示按 sort 升序即可）
     */
    public void removeItem(Long itemId) {
        requireItem(itemId);
        items.removeIf(i -> i.getId().equals(itemId));
    }

    /**
     * 已打包条目数
     */
    public long checkedCount() {
        return items.stream().filter(i -> i.getChecked() != null && i.getChecked() == 1).count();
    }

    /**
     * 仓储持久化清单头后回写主键（新建场景，条目随后由仓储逐条插入）
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 清单头内容校验
     */
    private void applyContent(String title, LocalDate hikeDate, String note) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("清单标题不能为空");
        }
        String t = title.trim();
        if (t.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("清单标题不能超过 100 字");
        }
        if (note != null && note.trim().length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("备注不能超过 200 字");
        }
        this.title = t;
        this.hikeDate = hikeDate;
        this.note = note == null || note.isBlank() ? null : note.trim();
    }

    /**
     * 定位本聚合内的条目（防跨清单操作）
     */
    private GearChecklistItem requireItem(Long itemId) {
        return items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("清单条目不存在"));
    }

    /**
     * 仓储重建：清单头 + 条目（条目已按 sort 升序）
     */
    public static GearChecklist reconstruct(Long id, Long userId, String title, LocalDate hikeDate, String note,
                                            Integer status, LocalDateTime createTime, LocalDateTime updateTime,
                                            List<GearChecklistItem> items) {
        GearChecklist checklist = new GearChecklist();
        checklist.id = id;
        checklist.userId = userId;
        checklist.title = title;
        checklist.hikeDate = hikeDate;
        checklist.note = note;
        checklist.status = status;
        checklist.createTime = createTime;
        checklist.updateTime = updateTime;
        checklist.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
        checklist.items.sort(Comparator.comparingInt(GearChecklistItem::getSort));
        return checklist;
    }
}
