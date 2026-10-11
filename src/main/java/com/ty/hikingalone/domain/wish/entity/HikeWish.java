package com.ty.hikingalone.domain.wish.entity;

import com.ty.hikingalone.common.enums.wish.WishPriorityEnum;
import com.ty.hikingalone.common.enums.wish.WishStatusEnum;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 徒步心愿领域实体（聚合根）
 * <p>状态机收口在聚合内：想去(0)/已计划(1) 为活动态，已完成(2)/已放弃(3) 为终态不可回退；
 * journal_id 的归属校验属跨实体规则，由应用服务完成</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HikeWish {

    /** 标题长度上限 */
    public static final int MAX_TITLE_LENGTH = 100;

    /** 地点描述长度上限 */
    public static final int MAX_LOCATION_LENGTH = 200;

    /** 备注长度上限 */
    public static final int MAX_NOTE_LENGTH = 500;

    private Long id;

    private Long userId;

    private String title;

    /**
     * 地点描述（可空，trim 后落库）
     */
    private String location;

    /**
     * 期望出发日期（可空；过期不强制处理，拖延是常态）
     */
    private LocalDate expectedDate;

    /**
     * 优先级（WishPriorityEnum.code：1 随缘 / 2 想去 / 3 一定要去）
     */
    private Integer priority;

    /**
     * 攻略/季节备忘（可空，trim 后落库）
     */
    private String note;

    /**
     * 状态（WishStatusEnum.code）
     */
    private Integer status;

    /**
     * 完成日期（完成时写入）
     */
    private LocalDate doneDate;

    /**
     * 完成后关联的日记 id（可空；归属校验在应用服务）
     */
    private Long journalId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 工厂方法：新建心愿，默认想去 + 想去优先级
     */
    public static HikeWish create(Long userId, String title, String location, LocalDate expectedDate,
                                  Integer priority, String note) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        HikeWish wish = new HikeWish();
        wish.userId = userId;
        wish.createTime = LocalDateTime.now();
        wish.applyContent(title, location, expectedDate, priority, note);
        wish.status = WishStatusEnum.WISH.getCode();
        return wish;
    }

    /**
     * 编辑心愿：非 null 字段覆盖；status 仅允许活动态之间切换（0↔1），
     * 终态条目不接受任何状态变更（完成/放弃走专用方法）
     */
    public void update(String title, String location, LocalDate expectedDate, Integer priority,
                       String note, Integer status) {
        if (isTerminal()) {
            if (status != null && !status.equals(this.status)) {
                throw new IllegalArgumentException("已完成的愿望不能再改状态");
            }
        } else if (status != null) {
            WishStatusEnum target = WishStatusEnum.fromCode(status);
            if (target != WishStatusEnum.WISH && target != WishStatusEnum.PLANNED) {
                throw new IllegalArgumentException("完成/放弃请使用专用接口");
            }
        }
        applyContent(
                title == null ? this.title : title,
                location, // null=不更新
                expectedDate,
                priority,
                note);
        if (status != null) {
            this.status = status;
        }
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记完成：活动态 → 已完成；doneDate 缺省今天且不允许未来；可挂日记
     */
    public void complete(LocalDate doneDate, Long journalId) {
        if (isTerminal()) {
            throw new IllegalArgumentException("该愿望已是终态");
        }
        LocalDate date = doneDate == null ? LocalDate.now() : doneDate;
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("完成日期不能是未来");
        }
        this.status = WishStatusEnum.DONE.getCode();
        this.doneDate = date;
        this.journalId = journalId;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 放弃：活动态 → 已放弃（保留原数据，不删历史）
     */
    public void giveUp() {
        if (isTerminal()) {
            throw new IllegalArgumentException("该愿望已是终态");
        }
        this.status = WishStatusEnum.GIVEN_UP.getCode();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 仓储持久化成功后回写生成的主键
     */
    public void attachId(Long id) {
        this.id = id;
    }

    /**
     * 是否终态（已完成/已放弃）
     */
    private boolean isTerminal() {
        return WishStatusEnum.DONE.getCode().equals(this.status)
                || WishStatusEnum.GIVEN_UP.getCode().equals(this.status);
    }

    /**
     * 内容组装与校验（update 场景传 null 表示保持原值：location/priority/note；
     * title/expectedDate 因工厂必填/可空语义直接全量接受）
     */
    private void applyContent(String title, String location, LocalDate expectedDate,
                              Integer priority, String note) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("标题不能超过 100 字");
        }
        if (location != null && location.trim().length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException("地点描述不能超过 200 字");
        }
        if (priority != null) {
            WishPriorityEnum.fromCode(priority);
        } else {
            priority = WishPriorityEnum.WANT.getCode();
        }
        if (note != null && note.trim().length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("备注不能超过 500 字");
        }
        this.title = t;
        if (location != null) {
            this.location = location.isBlank() ? null : location.trim();
        }
        this.expectedDate = expectedDate;
        this.priority = priority;
        if (note != null) {
            this.note = note.isBlank() ? null : note.trim();
        }
    }

    /**
     * 仓储重建：从持久化层还原聚合（不做业务校验，历史数据只读保真）
     */
    public static HikeWish reconstruct(Long id, Long userId, String title, String location,
                                       LocalDate expectedDate, Integer priority, String note,
                                       Integer status, LocalDate doneDate, Long journalId,
                                       LocalDateTime createTime, LocalDateTime updateTime) {
        HikeWish wish = new HikeWish();
        wish.id = id;
        wish.userId = userId;
        wish.title = title;
        wish.location = location;
        wish.expectedDate = expectedDate;
        wish.priority = priority;
        wish.note = note;
        wish.status = status;
        wish.doneDate = doneDate;
        wish.journalId = journalId;
        wish.createTime = createTime;
        wish.updateTime = updateTime;
        return wish;
    }
}
