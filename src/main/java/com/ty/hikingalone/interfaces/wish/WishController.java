package com.ty.hikingalone.interfaces.wish;

import com.ty.hikingalone.application.wish.WishService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.wish.converter.WishConverter;
import com.ty.hikingalone.interfaces.wish.dto.command.WishCompleteDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishIdDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishSaveDTO;
import com.ty.hikingalone.interfaces.wish.dto.command.WishUpdateDTO;
import com.ty.hikingalone.interfaces.wish.vo.query.WishSummaryVO;
import com.ty.hikingalone.interfaces.wish.vo.query.WishVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 徒步心愿单控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/wish")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;
    private final WishConverter wishConverter;

    /**
     * 新建心愿
     */
    @PostMapping
    public Result<WishVO> create(@Valid @RequestBody WishSaveDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(wishService.create(userId, wishConverter.toCmd(dto)));
    }

    /**
     * 编辑心愿（非 null 覆盖；状态仅 0↔1）
     */
    @PostMapping("/update")
    public Result<WishVO> update(@Valid @RequestBody WishUpdateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(wishService.update(userId, wishConverter.toCmd(dto)));
    }

    /**
     * 标记完成（可挂日记）
     */
    @PostMapping("/complete")
    public Result<WishVO> complete(@Valid @RequestBody WishCompleteDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(wishService.complete(userId, wishConverter.toCompleteCmd(dto)));
    }

    /**
     * 放弃
     */
    @PostMapping("/giveUp")
    public Result<WishVO> giveUp(@Valid @RequestBody WishIdDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(wishService.giveUp(userId, wishConverter.toIdCmd(dto)));
    }

    /**
     * 删除心愿
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody WishIdDTO dto) {
        wishService.delete(LoginUserContext.getUserId(), wishConverter.toIdCmd(dto));
        return Result.success();
    }

    /**
     * 全量心愿列表
     */
    @GetMapping("/list")
    public Result<List<WishVO>> list() {
        return Result.success(wishService.list(LoginUserContext.getUserId()));
    }

    /**
     * 汇总：各状态数量
     */
    @GetMapping("/summary")
    public Result<WishSummaryVO> summary() {
        return Result.success(wishService.summary(LoginUserContext.getUserId()));
    }
}
