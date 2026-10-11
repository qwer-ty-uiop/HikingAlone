package com.ty.hikingalone.interfaces.achievement;

import com.ty.hikingalone.application.achievement.AchievementService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.achievement.vo.query.AchievementGroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成就徽章控制器：只读接口
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * 全部徽章（按分组，实时计算达成状态）
     */
    @GetMapping("/all")
    public Result<List<AchievementGroupVO>> all() {
        return Result.success(achievementService.all(LoginUserContext.getUserId()));
    }
}
