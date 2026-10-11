package com.ty.hikingalone.interfaces.goal;

import com.ty.hikingalone.application.goal.GoalService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.goal.converter.GoalConverter;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalCreateDTO;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalIdDTO;
import com.ty.hikingalone.interfaces.goal.dto.command.GoalUpdateDTO;
import com.ty.hikingalone.interfaces.goal.vo.query.GoalVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 目标模块控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/goal")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final GoalConverter goalConverter;

    /**
     * 新建目标（同期同指标查重）
     */
    @PostMapping
    public Result<GoalVO> create(@Valid @RequestBody GoalCreateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(goalService.create(userId, goalConverter.toCreateCmd(dto)));
    }

    /**
     * 编辑目标（只许改 target/status）
     */
    @PostMapping("/update")
    public Result<GoalVO> update(@Valid @RequestBody GoalUpdateDTO dto) {
        Long userId = LoginUserContext.getUserId();
        return Result.success(goalService.update(userId, goalConverter.toUpdateCmd(dto)));
    }

    /**
     * 删除目标
     */
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody GoalIdDTO dto) {
        goalService.delete(LoginUserContext.getUserId(), goalConverter.toIdCmd(dto));
        return Result.success();
    }

    /**
     * 某年目标列表（含读时进度）；year 缺省当前年
     */
    @GetMapping("/list")
    public Result<List<GoalVO>> list(@RequestParam(required = false) Integer year) {
        return Result.success(goalService.list(LoginUserContext.getUserId(), year));
    }
}
