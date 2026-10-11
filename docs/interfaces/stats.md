# 年度统计模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

年度统计是徒步数据的**年终报告页**：一年走了多少公里、爬了多少米、每个月的分布、最远的一天。与成就徽章的区别：徽章看「里程碑」，统计看「一年全貌」；与目标模块的区别：目标看「某周期的达成」，统计只陈述事实。

约束：需登录、只用 `GET`、统一 `Result<T>`。

## 设计决策

- **零表设计**：与成就模块同理，全部读时从 hike_journal 聚合，个人数据量级下无需预聚合表
- 取代此前 `/profile` 的 WIP 骨架（该骨架从未注册任何端点，本次一并清理）

## 领域结构（DDD 四层）

```
domain/stats
  └── entity/YearlyStats.java              # 值对象：年度汇总 + 月度分布（由应用服务组装）

application/stats
  └── StatsService.java                    # 依赖 HikeJournalRepository 聚合年度数据

interfaces/stats
  ├── StatsController.java
  ├── converter/StatsConverter.java
  └── vo/query/YearlyStatsVO.java / MonthlyPointVO.java / AvailableYearVO.java
```

## 接口清单

### 1. GET /stats/annual?year=2026 —— 年度报告

`year` 缺省当前年，钳制 [2020, 2100]。

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "year": 2026,
    "hikeCount": 8,
    "totalDistanceKm": 121.3,
    "totalDurationMin": 2640,
    "totalElevationGain": 5320.0,
    "totalCost": 860.0,
    "maxSingleDistanceKm": 22.5,
    "maxSingleElevationGain": 1250.0,
    "firstHikeDate": "2026-03-14",
    "monthly": [
      { "month": 1, "hikeCount": 0, "distanceKm": 0 },
      { "month": 3, "hikeCount": 1, "distanceKm": 8.2 },
      { "month": 10, "hikeCount": 3, "distanceKm": 46.1 }
    ]
  }
}
```

`monthly` 固定返回 1~12 月（无徒步的月 hikeCount=0），前端直接画柱状图。

### 2. GET /stats/years —— 可选年份列表

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [ { "year": 2026, "hikeCount": 8 }, { "year": 2025, "hikeCount": 4 } ]
}
```

来源 = 有日记的年份 ∪ 当前年，按年份倒序；每年附带徒步次数供切换器展示。

## 业务规则

- 全部归属过滤，只统计当前用户
- 某年无日记：返回全 0 结构（monthly 仍 12 个月），前端显示空态
- 错误返回：未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：GET /stats/annual、GET /stats/years；零表读时聚合；同步清理 /profile WIP 死代码 |
