package com.ty.hikingalone.interfaces.journal;

import com.ty.hikingalone.application.journal.JournalService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.journal.converter.JournalConverter;
import com.ty.hikingalone.interfaces.journal.dto.command.JournalIdDTO;
import com.ty.hikingalone.interfaces.journal.dto.command.JournalSaveDTO;
import com.ty.hikingalone.interfaces.journal.dto.command.JournalUpdateDTO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalDetailVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalSummaryVO;
import com.ty.hikingalone.interfaces.journal.vo.query.JournalVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 徒步日记模块控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;
    private final JournalConverter journalConverter;

    /**
     * 新建日记（关联轨迹可自动带出缺失指标）
     */
    @PostMapping
    public Result<JournalVO> create(@Valid @RequestBody JournalSaveDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(journalService.create(userId, journalConverter.toCmd(dto)));
    }

    /**
     * 编辑日记（全量替换记述内容）
     */
    @PostMapping("/update")
    public Result<JournalVO> update(@Valid @RequestBody JournalUpdateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(journalService.update(userId, journalConverter.toCmd(dto)));
    }

    /**
     * 删除日记（物理删除，不影响关联轨迹）
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody JournalIdDTO dto) {
        journalService.delete(LoginUserContext.getUserId(), dto.getId());
        return Result.success();
    }

    /**
     * 最近日记列表（limit 缺省 50，钳制 [1, 200]）
     */
    @GetMapping("/list")
    public Result<List<JournalVO>> list(@RequestParam(required = false) Integer limit) {
        return Result.success(journalService.listRecent(LoginUserContext.getUserId(), limit));
    }

    /**
     * 日记详情（完整正文）
     */
    @GetMapping("/{id}")
    public Result<JournalDetailVO> detail(@PathVariable Long id) {
        return Result.success(journalService.detail(LoginUserContext.getUserId(), id));
    }

    /**
     * 汇总统计（累计次数/里程/爬升/花费与年度概览）
     */
    @GetMapping("/summary")
    public Result<JournalSummaryVO> summary() {
        return Result.success(journalService.summary(LoginUserContext.getUserId()));
    }
}
