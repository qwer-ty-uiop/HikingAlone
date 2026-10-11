# 目标模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

目标模块回答「这个月/这一年我要走多少」。用户设定一个周期内的量化目标（月度或年度），进度由**徒步日记数据自动累计**，无需手动打卡——目标是日记的另一种读法。达成与否在读取时计算，不写库，保证「补记 9 月日记」这类操作天然能修正历史进度。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `hike_goal` | 目标 | user_id, metric, target_value, period_type, year, month(0=年), status |

取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `metric` | [1, 6] | 1 月徒步里程 km / 2 月徒步次数 / 3 月累计爬升 m / 4 年徒步里程 km / 5 年徒步次数 / 6 年累计爬升 m |
| `target_value` | (0, 100000] | 1 位小数；里程 ≤ 5000、次数 ≤ 365、爬升 ≤ 200000 在实体内按 metric 分别钳制 |
| `period_type` | 1 月度 / 2 年度 | |
| `year` | [2020, 2100] | |
| `month` | [0, 12]；月度 [1, 12]，年度固定 0 | 0 用作年度哨兵，保证唯一索引可用 |
| `status` | 0 已放弃 / 1 进行中 | 达成/未达成是**读时计算**，不落库 |

唯一约束：`uk_user_period_metric (user_id, year, month, metric)` —— 同期同指标只能有一个目标（应用层先查后写 + DB 兜底）。
索引：`idx_user_id (user_id)`。

进度来源（应用服务读取时聚合 hike_journal）：

| metric | 聚合方式 |
|---|---|
| 1 / 4 | 该周期内日记 `distance_km` 求和（null 记 0） |
| 2 / 5 | 该周期内日记条数 |
| 3 / 6 | 该周期内日记 `elevation_gain` 求和（null 记 0） |

## 领域结构（DDD 四层）

```
domain/goal
  ├── entity/HikeGoal.java                 # 聚合根：工厂 create()（含 target 钳制）/ update()（只许改 target/status）
  └── repository/HikeGoalRepository.java   # save / findById / listByUserAndYear / delete

application/goal
  ├── GoalService.java                     # 编排：查重、跨模块取日记聚合、进度计算
  └── cmd/GoalCreateCmd.java / GoalUpdateCmd.java

infrastructure/goal
  ├── mapper/HikeGoalMapper.java
  ├── po/HikeGoalPO.java
  └── repository/impl/HikeGoalRepositoryImpl.java

interfaces/goal
  ├── GoalController.java
  ├── converter/GoalConverter.java
  ├── dto/command/GoalCreateDTO.java / GoalUpdateDTO.java / GoalIdDTO.java
  └── vo/query/GoalVO.java（含进度）
```

分层说明：`metric`/`period` 创建后不可变（update 只接受 targetValue/status），避免「改目标口径」导致的进度语义漂移；进度计算依赖日记仓储，属应用服务编排。

## 接口清单

### 1. POST /goal —— 新建目标

```json
{ "metric": 1, "targetValue": 80, "year": 2026, "month": 10 }
```

同期同指标已存在 → 400「该周期已存在同类型目标」。响应：GoalVO（含进度）。

### 2. POST /goal/update —— 修改目标

请求体 `{ id, targetValue?, status? }`（非 null 覆盖）。metric/period/year/month 不可变。

### 3. POST /goal/delete —— 删除目标

请求体 `{ id }`。物理删除。

### 4. GET /goal/list?year=2026 —— 年度目标列表（含进度）

返回该用户 `year` 年的所有目标（月度 + 年度），每个目标附带进度：

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [{
    "id": 1, "metric": 1, "metricLabel": "月徒步里程",
    "targetValue": 80, "periodType": 1, "year": 2026, "month": 10,
    "status": 1,
    "progress": {
      "current": 46.3,
      "percent": 58,
      "achieved": false,
      "periodEnded": false,
      "unit": "km"
    }
  }]
}
```

排序：`period_type` 降序（年度在前）→ `month` 升序 → `metric` 升序。`percent` 钳制 [0, 999]（超额显示真实超额倍数，前端截断展示）。

## 业务规则

- 补记历史日记后，历史周期目标的进度随之变化（读时计算的红利）
- 已放弃（status=0）的目标仍返回并显示当前累计值，方便「复活」评估；是否展示由前端决定
- 错误返回：参数校验/查重冲突 400；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /goal、/goal/update、/goal/delete + GET /goal/list（读时聚合日记进度）；uk_user_period_metric 防重复目标 |
