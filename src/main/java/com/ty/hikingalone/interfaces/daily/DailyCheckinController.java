package com.ty.hikingalone.interfaces.daily;

import com.ty.hikingalone.application.daily.DailyCheckinService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.daily.converter.DailyCheckinConverter;
import com.ty.hikingalone.interfaces.daily.dto.command.DailyCheckinDTO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyCheckinVO;
import com.ty.hikingalone.interfaces.daily.vo.query.DailyRecentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日打卡模块控制器：接口层，只做 HTTP 适配
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/daily")
@RequiredArgsConstructor
public class DailyCheckinController {

    private final DailyCheckinService dailyCheckinService;
    private final DailyCheckinConverter dailyCheckinConverter;

    /**
     * 打卡（部分更新：只覆盖非 null 字段；同日多次打卡互不干扰）
     */
    @PostMapping("/checkin")
    public Result<DailyCheckinVO> checkin(@Valid @RequestBody DailyCheckinDTO dto) {
        return Result.success(dailyCheckinService.checkin(
                LoginUserContext.getUserId(), dailyCheckinConverter.toCmd(dto)));
    }

    /**
     * 最近 N 天记录（升序）+ 汇总统计；days 缺省 30
     */
    @GetMapping("/recent")
    public Result<DailyRecentVO> recent(@RequestParam(required = false) Integer days) {
        return Result.success(dailyCheckinService.recent(LoginUserContext.getUserId(), days));
    }
}
