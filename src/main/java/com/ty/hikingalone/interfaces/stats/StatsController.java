package com.ty.hikingalone.interfaces.stats;

import com.ty.hikingalone.application.stats.StatsService;
import com.ty.hikingalone.common.context.LoginUserContext;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.interfaces.stats.vo.query.AvailableYearVO;
import com.ty.hikingalone.interfaces.stats.vo.query.YearlyStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 年度统计控制器：只读接口
 * <p>需登录（LoginUserInterceptor 拦截），当前用户 id 从登录会话上下文取</p>
 */
@Slf4j
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 年度报告（year 缺省当前年）
     */
    @GetMapping("/annual")
    public Result<YearlyStatsVO> annual(@RequestParam(required = false) Integer year) {
        return Result.success(statsService.annual(LoginUserContext.getUserId(), year));
    }

    /**
     * 可选年份列表（年份切换器数据源）
     */
    @GetMapping("/years")
    public Result<List<AvailableYearVO>> years() {
        return Result.success(statsService.years(LoginUserContext.getUserId()));
    }
}
