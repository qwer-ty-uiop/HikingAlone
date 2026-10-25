# 健康检查模块接口设计

## 概述

`GET /health`：运维探活端点，探测 MySQL 与 Redis 连通性并返回组件级状态。

- **裸 JSON，不套业务 `Result<T>` 包装**：探活由 nginx / CD / 外部监控消费，约定俗成读 HTTP 状态码，业务包装反而碍事（`/health` 是全项目唯一例外，业务接口仍走 `Result`）
- **公开端点**：不在 `LoginUserInterceptor` 拦截清单内，无需登录——探活不该依赖会话
- 无业务语义，不建领域层：单 Controller 直接探测（健康检查不是业务用例，DDD 分层在此刻意从简）

## 接口清单

### GET /health —— 组件健康

无请求参数，无需登录。

- 全部组件正常：HTTP **200**，`status: "UP"`
- 任一组件异常：HTTP **503**，`status: "DOWN"`（该组件 `status: "DOWN"` 并附 `error` 摘要）

响应 `data`：

```json
{
  "status": "UP",
  "components": {
    "mysql": { "status": "UP", "latencyMs": 2 },
    "redis": { "status": "UP", "latencyMs": 1 }
  }
}
```

探测方式：MySQL 执行 `SELECT 1`（JdbcTemplate）；Redis 执行 `PING`（StringRedisTemplate 连接工厂）。`latencyMs` 为探测耗时，仅观测用。

## 消费方

- **CD 部署自检**（后端 ci.yml deploy 步骤）：`systemctl restart` 后重试探测 `/health`，非 `status:"UP"` 直接把部署标红
- **nginx**：`/health` 前缀反代到后端（`deploy/nginx-hikingalone.conf`），外部监控可经 `http://<host>/health` 探活
- 前端登录守卫、业务请求**不使用**本端点

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-25 | v1 定稿：MySQL/Redis 双探测，200/503 语义，CD 强校验 + nginx 探活接入 |
