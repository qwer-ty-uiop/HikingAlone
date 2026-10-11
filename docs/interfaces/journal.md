# 徒步日记模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

徒步日记是「一人徒」的核心叙事模块：每次徒步结束后，记录一段**带上下文的回顾**——路线、距离、爬升这些数字已经由轨迹模块（route_track）解决，日记回答的是数字之外的问题：那天天气如何、跟谁去（或独自）、花了多少钱、感受怎样。训练是日常输入，轨迹是客观输出，日记是主观沉淀。

约束（与训练/身体模块一致）：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `hike_journal` | 徒步日记（每次徒步一条，可编辑） | user_id, title, hike_date, route_id?, distance_km?, duration_min?, elevation_gain?, difficulty, mood, weather?, companions?, cost?, content |

**为什么不是 append 事件流**：日记是「一次徒步」的完整记述，事后补记/修改是正常诉求（回家想起来漏写天气），语义上是修正而非新事件，因此按聚合主键覆盖更新，`update_time` 留痕。

字段取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `title` | [1, 100] 字符 | 如「雨后初晴 · 海坨山南大梁」 |
| `hike_date` | ≤ 今天 | 日记是回顾性的；未来出行计划归心愿单模块 |
| `route_id` | 可空；非空必须属于当前用户 | 关联轨迹模块，越权关联直接拒绝（跨实体校验，放应用服务） |
| `distance_km` | 可空；非空 (0, 500]，2 位小数 | 公里；关联轨迹且未填时自动从轨迹带出 |
| `duration_min` | 可空；非空 [1, 4320] | 分钟，上限 3 天（连续多日徒步按单日记） |
| `elevation_gain` | 可空；非空 [0, 10000]，1 位小数 | 累计爬升米 |
| `difficulty` | [1, 5] | 1 休闲 / 2 轻松 / 3 进阶 / 4 挑战 / 5 地狱 |
| `mood` | [1, 5] | 1 疲惫 / 2 平静 / 3 愉悦 / 4 兴奋 / 5 震撼 |
| `weather` | ≤ 50 字符 | 自由文本：「多云 12℃ 微风」 |
| `companions` | ≤ 100 字符 | 「独行」或同伴名 |
| `cost` | 可空；非空 [0, 999999.99] | 本次总花费（交通+食宿+门票） |
| `content` | ≤ 8000 字符 | 正文，trim 后落库 |

索引：`idx_user_date (user_id, hike_date)`（按日期倒序列表）。

## 领域结构（DDD 四层）

```
domain/journal
  └── entity/HikeJournal.java              # 聚合根：工厂 record() / 方法 update() 内聚不变量
  └── repository/HikeJournalRepository.java # save / findById / listByUser(limit) / listByUserAndYear / delete

application/journal
  ├── JournalService.java                  # 编排：route 归属校验 + 缺失指标自动带出 + 汇总统计
  └── cmd/JournalCmd.java                  # { id?, userId, title, hikeDate, routeId?, distanceKm?,
                                           #   durationMin?, elevationGain?, difficulty, mood,
                                           #   weather?, companions?, cost?, content? }

infrastructure/journal
  ├── mapper/HikeJournalMapper.java
  ├── po/HikeJournalPO.java
  └── repository/impl/HikeJournalRepositoryImpl.java

interfaces/journal
  ├── JournalController.java
  ├── converter/JournalConverter.java
  ├── dto/command/JournalSaveDTO.java / JournalIdDTO.java
  └── vo/query/JournalVO.java / JournalDetailVO.java / JournalSummaryVO.java
```

分层说明：

- 单条日记的区间校验（距离/时长/爬升/难度/心情）在聚合根工厂与 `update()`
- **route_id 归属校验**与「关联轨迹自动带出缺失指标」跨实体，在应用服务完成（与 DDD 校验分层约定一致）
- 汇总统计（总次数/总里程/年度里程）跨多条记录，在应用服务计算

## 接口清单

### 1. POST /journal —— 新建日记

请求体：

```json
{
  "title": "雨后初晴 · 海坨山南大梁",
  "hikeDate": "2026-10-11",
  "routeId": 3,
  "distanceKm": null,
  "durationMin": null,
  "elevationGain": null,
  "difficulty": 3,
  "mood": 5,
  "weather": "多云 12℃ 微风",
  "companions": "独行",
  "cost": 86.5,
  "content": "雨后的山脊线干净得像刚洗过……"
}
```

规则：`routeId` 非空时校验轨迹归属（不属于当前用户 → 400「关联的路线不存在」）；`distanceKm`/`durationMin`/`elevationGain` 为 null 且 routeId 有效时，**自动从轨迹带出**（响应返回带出后的值，前端无需二次请求）。

响应：落库后的日记 VO（含 `routeName`）。

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "id": 1, "title": "雨后初晴 · 海坨山南大梁", "hikeDate": "2026-10-11",
    "routeId": 3, "routeName": "海坨山南大梁环穿",
    "distanceKm": 18.60, "durationMin": 420, "elevationGain": 1250.0,
    "difficulty": 3, "mood": 5, "weather": "多云 12℃ 微风",
    "companions": "独行", "cost": 86.5,
    "contentPreview": "雨后的山脊线干净得像刚洗过……"
  }
}
```

### 2. POST /journal/update —— 编辑日记

请求体同新建，额外必填 `id`。同样支持改绑 `routeId`（含归属校验与带出逻辑；显式传入的指标不被覆盖）。

### 3. POST /journal/delete —— 删除日记

请求体 `{ "id": 1 }`。物理删除（学习项目，无回收站）。

### 4. GET /journal/list?limit=50 —— 最近日记列表

`limit` 钳制 [1, 200]。按 `hike_date` 倒序、同日按 `id` 倒序。`content` 截断为 `contentPreview`（≤120 字）。响应 `data` 为 VO 数组。

### 5. GET /journal/{id} —— 日记详情

返回完整 `content`。不存在或不属于当前用户 → 400「日记不存在」。

### 6. GET /journal/summary —— 汇总统计

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "totalCount": 12,
    "totalDistanceKm": 186.4,
    "totalDurationMin": 3960,
    "totalElevationGain": 8320.0,
    "totalCost": 1234.5,
    "thisYearCount": 8,
    "thisYearDistanceKm": 121.3,
    "maxSingleDistanceKm": 22.5,
    "lastHikeDate": "2026-10-11"
  }
}
```

全部从当前用户日记聚合；无日记时数值为 0 / null（`lastHikeDate` null）。

## 业务规则

- 列表与详情都做归属过滤，他人的日记不可见、不可改
- 删除日记**不**影响关联的轨迹本身；轨迹被删后日记的 routeId 悬空，展示时 `routeName` 为 null（不级联清理，保留历史语义）
- 错误返回：参数校验 400；未登录 401（拦截器）；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /journal、/journal/update、/journal/delete + GET /journal/list、/journal/{id}、/journal/summary；关联轨迹自动带出里程/时长/爬升 |
