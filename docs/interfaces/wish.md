# 徒步心愿单模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

心愿单承接「下次去哪」：刷到的线路、朋友种草的山头、憋了很久的长线，先记下来，再逐步从「想去」推进到「已计划」，走完之后标记「已完成」并可挂上对应的徒步日记，形成 **种草 → 计划 → 走完 → 回顾** 的闭环。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `hike_wish` | 心愿条目 | user_id, title, location?, expected_date?, priority, note?, status, done_date?, journal_id? |

取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `title` | [1, 100] 字符 | 如「武功山金顶看日出」 |
| `location` | ≤ 200 字符 | 地点描述：「江西 · 萍乡」 |
| `expected_date` | 可空 | 期望出发时间（可以只填月份的模糊期望，用 note 补充） |
| `priority` | [1, 3] | 1 随缘 / 2 想去 / 3 一定要去 |
| `note` | ≤ 500 字符 | 路线攻略备忘、季节提示等 |
| `status` | 0 想去 / 1 已计划 / 2 已完成 / 3 已放弃 | |
| `done_date` | 完成时自动/可指定 | ≤ 今天 |
| `journal_id` | 可空；非空必须属于当前用户 | 完成后关联的日记（跨实体校验放应用服务） |

状态流转（聚合根 `complete()`/`giveUp()` 内聚）：

```
0 想去 ──┬──> 1 已计划 ──┬──> 2 已完成（done_date + 可选 journal_id）
         │               └──> 3 已放弃
         └────────────────> 2 已完成 / 3 已放弃
```

- 已完成/已放弃为终态，不允许再改回（update 不接受对终态条目改 status）
- `idx_user_id (user_id)`

## 领域结构（DDD 四层）

```
domain/wish
  └── entity/HikeWish.java                 # 聚合根：工厂 create() / update() / complete() / giveUp()
  └── repository/HikeWishRepository.java   # save / findById / listByUser / delete

application/wish
  ├── WishService.java                     # 编排：journal 归属校验、状态流转调用
  └── cmd/WishCmd.java / WishCompleteCmd.java

infrastructure/wish
  ├── mapper/HikeWishMapper.java
  ├── po/HikeWishPO.java
  └── repository/impl/HikeWishRepositoryImpl.java

interfaces/wish
  ├── WishController.java
  ├── converter/WishConverter.java
  ├── dto/command/WishSaveDTO.java / WishIdDTO.java / WishCompleteDTO.java
  └── vo/query/WishVO.java / WishSummaryVO.java
```

## 接口清单

### 1. POST /wish —— 新建心愿

```json
{ "title": "武功山金顶看日出", "location": "江西 · 萍乡", "expectedDate": "2026-11-01", "priority": 3, "note": "避开周末，秋高气爽" }
```

新建即 status=0 想去。响应：落库后的 WishVO。

### 2. POST /wish/update —— 编辑心愿

请求体：`{ id, title?, location?, expectedDate?, priority?, note?, status? }`（非 null 覆盖）。
status 仅允许 0↔1 之间切换（想去↔已计划）；置 2/3 必须走 /wish/complete、/wish/giveUp。对终态条目改 status → 400「已完成的愿望不能再改状态」。

### 3. POST /wish/complete —— 标记完成

```json
{ "id": 1, "doneDate": "2026-10-20", "journalId": 8 }
```

`doneDate` 缺省今天且必须 ≤ 今天；`journalId` 可空，非空时校验日记归属。响应更新后的 WishVO。

### 4. POST /wish/giveUp —— 放弃

请求体 `{ "id": 1 }`。status → 3，保留原数据（不删历史）。

### 5. POST /wish/delete —— 删除

请求体 `{ "id": 1 }`。物理删除（含已完成/已放弃的）。

### 6. GET /wish/list —— 全量列表

按 `status` 升序（想去→已计划→已完成→已放弃）、同状态内 `priority` 降序 + `expected_date` 升序。响应 `data` 为 WishVO 数组（journalId 附带 `journalTitle`，未关联为 null）。

### 7. GET /wish/summary —— 汇总

```json
{ "wishCount": 4, "plannedCount": 2, "doneCount": 7, "givenUpCount": 1 }
```

## 业务规则

- 列表/详情/修改均归属过滤；他人的心愿不可见
- 完成时挂 journal 的校验在应用服务（跨实体），聚合根只保证自身状态合法
- 错误返回：参数校验/状态非法 400；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /wish、/wish/update、/wish/complete、/wish/giveUp、/wish/delete + GET /wish/list、/wish/summary；完成可挂日记，状态机收口在聚合根 |
