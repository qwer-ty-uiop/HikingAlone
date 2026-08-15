package com.ty.hikingalone.controller.index;


import com.ty.hikingalone.application.service.IndexService;
import com.ty.hikingalone.common.result.Result;
import com.ty.hikingalone.controller.index.vo.HomeBodyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器：接口层，只做 HTTP 适配，不包含业务逻辑
 */
@Slf4j
@ResponseBody
@RestController
@RequiredArgsConstructor
public class IndexController {

    private final IndexService indexService;

    /**
     * 获取首页数据
     */
    @GetMapping("/home")
    public Result<HomeBodyVO> home() {
        log.info("获取首页数据");
        return Result.success(indexService.getHomeBody());
    }

}
