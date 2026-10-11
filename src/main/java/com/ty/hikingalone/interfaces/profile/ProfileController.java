package com.ty.hikingalone.interfaces.profile;

import com.ty.hikingalone.application.profile.ProfileService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.profile.vo.query.DashboardVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心（驾驶舱）控制器：只读聚合接口
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 驾驶舱聚合：今日打卡 + 待办提醒 + 徒步概览 + 连续记录 + 最近日记/进行中目标/心愿/徽章
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(profileService.dashboard(LoginUserContext.getUserId()));
    }
}
