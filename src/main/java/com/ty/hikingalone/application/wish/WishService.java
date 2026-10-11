package com.ty.hikingalone.application.wish;

import com.ty.hikingalone.application.wish.cmd.WishCmd;
import com.ty.hikingalone.application.wish.cmd.WishCompleteCmd;
import com.ty.hikingalone.application.wish.cmd.WishIdCmd;
import com.ty.hikingalone.common.enums.wish.WishStatusEnum;
import com.ty.hikingalone.domain.journal.entity.HikeJournal;
import com.ty.hikingalone.domain.journal.repository.HikeJournalRepository;
import com.ty.hikingalone.domain.wish.entity.HikeWish;
import com.ty.hikingalone.domain.wish.repository.HikeWishRepository;
import com.ty.hikingalone.interfaces.wish.converter.WishConverter;
import com.ty.hikingalone.interfaces.wish.vo.query.WishSummaryVO;
import com.ty.hikingalone.interfaces.wish.vo.query.WishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 徒步心愿应用服务：日期解析、关联日记归属校验与标题回显、汇总统计
 * （状态机与内容不变量在聚合根内）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishService {

    private final HikeWishRepository wishRepository;
    private final HikeJournalRepository journalRepository;
    private final WishConverter converter;

    /**
     * 新建心愿（默认想去）
     */
    @Transactional
    public WishVO create(Long userId, WishCmd cmd) {
        HikeWish wish = HikeWish.create(userId, cmd.getTitle(), cmd.getLocation(),
                parseDate(cmd.getExpectedDate()), cmd.getPriority(), cmd.getNote());
        wish.attachId(wishRepository.save(wish));
        return converter.toVO(wish, Map.of());
    }

    /**
     * 编辑心愿（非 null 覆盖；状态仅 0↔1）
     */
    @Transactional
    public WishVO update(Long userId, WishCmd cmd) {
        HikeWish existing = requireOwnedWish(userId, cmd.getId());
        existing.update(cmd.getTitle(), cmd.getLocation(), parseDate(cmd.getExpectedDate()),
                cmd.getPriority(), cmd.getNote(), cmd.getStatus());
        wishRepository.save(existing);
        return converter.toVO(existing, journalTitleMap(existing));
    }

    /**
     * 标记完成（可挂日记；日记必须属于当前用户）
     */
    @Transactional
    public WishVO complete(Long userId, WishCompleteCmd cmd) {
        HikeWish existing = requireOwnedWish(userId, cmd.getId());
        Long journalId = cmd.getJournalId();
        if (journalId != null) {
            HikeJournal journal = journalRepository.findById(journalId);
            if (journal == null || !userId.equals(journal.getUserId())) {
                throw new IllegalArgumentException("关联的日记不存在");
            }
        }
        existing.complete(parseDate(cmd.getDoneDate()), journalId);
        wishRepository.save(existing);
        return converter.toVO(existing, journalTitleMap(existing));
    }

    /**
     * 放弃（保留历史数据）
     */
    @Transactional
    public WishVO giveUp(Long userId, WishIdCmd cmd) {
        HikeWish existing = requireOwnedWish(userId, cmd.getId());
        existing.giveUp();
        wishRepository.save(existing);
        return converter.toVO(existing, journalTitleMap(existing));
    }

    /**
     * 删除心愿（物理删除）
     */
    @Transactional
    public void delete(Long userId, WishIdCmd cmd) {
        requireOwnedWish(userId, cmd.getId());
        wishRepository.delete(cmd.getId());
    }

    /**
     * 全量心愿列表
     */
    public List<WishVO> list(Long userId) {
        List<HikeWish> wishes = wishRepository.listByUser(userId);
        Map<Long, String> journalTitles = new HashMap<>();
        for (HikeWish w : wishes) {
            collectJournalTitle(journalTitles, w.getJournalId());
        }
        return converter.toVOList(wishes, journalTitles);
    }

    /**
     * 汇总：各状态数量
     */
    public WishSummaryVO summary(Long userId) {
        List<HikeWish> wishes = wishRepository.listByUser(userId);
        WishSummaryVO summary = new WishSummaryVO();
        for (HikeWish w : wishes) {
            Integer status = w.getStatus();
            if (WishStatusEnum.WISH.getCode().equals(status)) {
                summary.setWishCount(summary.getWishCount() + 1);
            } else if (WishStatusEnum.PLANNED.getCode().equals(status)) {
                summary.setPlannedCount(summary.getPlannedCount() + 1);
            } else if (WishStatusEnum.DONE.getCode().equals(status)) {
                summary.setDoneCount(summary.getDoneCount() + 1);
            } else if (WishStatusEnum.GIVEN_UP.getCode().equals(status)) {
                summary.setGivenUpCount(summary.getGivenUpCount() + 1);
            }
        }
        return summary;
    }

    /**
     * 归属校验：不存在或不属于当前用户统一报「心愿不存在」
     */
    private HikeWish requireOwnedWish(Long userId, Long id) {
        if (id == null) {
            throw new IllegalArgumentException("心愿 id 不能为空");
        }
        HikeWish wish = wishRepository.findById(id);
        if (wish == null || !userId.equals(wish.getUserId())) {
            throw new IllegalArgumentException("心愿不存在");
        }
        return wish;
    }

    /**
     * 批量补齐关联日记标题（展示用）
     */
    private Map<Long, String> journalTitleMap(HikeWish wish) {
        Map<Long, String> titles = new HashMap<>();
        collectJournalTitle(titles, wish.getJournalId());
        return titles;
    }

    private void collectJournalTitle(Map<Long, String> titles, Long journalId) {
        if (journalId == null || titles.containsKey(journalId)) {
            return;
        }
        HikeJournal journal = journalRepository.findById(journalId);
        titles.put(journalId, journal == null ? null : journal.getTitle());
    }

    /**
     * 解析日期：空=null；格式错误转 400
     */
    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("日期格式错误，应为 yyyy-MM-dd");
        }
    }
}
