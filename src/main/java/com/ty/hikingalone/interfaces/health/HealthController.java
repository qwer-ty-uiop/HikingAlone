package com.ty.hikingalone.interfaces.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查端点：探测 MySQL/Redis 连通性，供 nginx 探活与 CD 部署自检消费。
 * <p>裸 JSON + HTTP 200/503 语义，不套业务 {@code Result<T>} 包装（全项目唯一例外，
 * 见 docs/interfaces/health.md）；公开端点，不在登录拦截清单内。
 * 健康检查不是业务用例，不建领域层，单控制器直探测。</p>
 */
@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> mysql = probe("mysql",
                () -> jdbcTemplate.queryForObject("SELECT 1", Integer.class));
        Map<String, Object> redis = probe("redis",
                () -> redisTemplate.getConnectionFactory().getConnection().ping());

        Map<String, Object> body = aggregate(mysql, redis);
        boolean up = "UP".equals(body.get("status"));
        return ResponseEntity.status(up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    /** 探测动作：可能抛受检异常的零参操作 */
    @FunctionalInterface
    interface ProbeAction {
        void run() throws Exception;
    }

    /** 单组件探测：成功 UP + 耗时；失败 DOWN + 错误摘要（不抛出，探活端点自身不能 500） */
    static Map<String, Object> probe(String name, ProbeAction action) {
        Map<String, Object> result = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        try {
            action.run();
            result.put("status", "UP");
            result.put("latencyMs", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.warn("健康检查组件异常: {} -> {}", name, e.getMessage());
            result.put("status", "DOWN");
            result.put("error", String.valueOf(e.getMessage()).lines().findFirst().orElse(e.getClass().getSimpleName()));
        }
        return result;
    }

    /** 汇总：任一组件 DOWN 则整体 DOWN */
    static Map<String, Object> aggregate(Map<String, Object> mysql, Map<String, Object> redis) {
        boolean up = "UP".equals(mysql.get("status")) && "UP".equals(redis.get("status"));
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("mysql", mysql);
        components.put("redis", redis);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", up ? "UP" : "DOWN");
        body.put("components", components);
        return body;
    }
}
