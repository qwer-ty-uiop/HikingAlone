package com.ty.hikingalone.interfaces.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 健康检查聚合逻辑单测：不启 Spring 上下文，直接验证 probe/aggregate 的状态语义
 */
class HealthControllerTest {

    @Test
    void probeSuccessReportsUpWithLatency() {
        Map<String, Object> result = HealthController.probe("mysql", () -> { });

        assertThat(result.get("status")).isEqualTo("UP");
        assertThat((Long) result.get("latencyMs")).isGreaterThanOrEqualTo(0);
        assertThat(result).doesNotContainKey("error");
    }

    @Test
    void probeFailureReportsDownWithError() {
        Map<String, Object> result = HealthController.probe("redis",
                () -> { throw new IllegalStateException("Connection refused"); });

        assertThat(result.get("status")).isEqualTo("DOWN");
        assertThat((String) result.get("error")).isEqualTo("Connection refused");
    }

    @Test
    void aggregateUpOnlyWhenAllComponentsUp() {
        Map<String, Object> up = HealthController.probe("a", () -> { });
        Map<String, Object> down = HealthController.probe("b",
                () -> { throw new RuntimeException("boom"); });

        assertThat(HealthController.aggregate(up, up).get("status")).isEqualTo("UP");
        assertThat(HealthController.aggregate(up, down).get("status")).isEqualTo("DOWN");
        assertThat(HealthController.aggregate(down, up).get("status")).isEqualTo("DOWN");
    }
}
