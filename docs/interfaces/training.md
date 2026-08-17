# 训练模块接口设计

> 文档版本：v1 · 2026-08-16 · 状态：已实现（后端已按此设计编码）

## 概述

训练模块用于日常训练与徒步训练：用户自建周期性训练计划（如"100 个俯卧撑"），周期内每天通过滑动条/输入框提交完成情况，并以日历热力图展示打卡记录。

约束：

- 暂无登录体系，统一固定用户 `user_id = 1`
- 只用 `GET` / `POST` 两种方法
- 所有响应统一 `Result<T>` 格式
- 前端只允许调用后端已实现的接口

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `training_plan` | 训练计划 | title, description, start_date, end_date, status, cycle_type, cycle_anchor |
| `training_plan_item` | 计划训练项（一个计划多个项） | name, mode(times/sets), total_times, total_sets, unit, sort |
| `training_record` | 提交事件表（**每次提交追加一条**，事件流，同日可多条，不可变日志） | plan_id, item_id, record_date, completed_sets, completed_times, create_time |
| `training_record_daily` | 每日汇总表（**每训练项每天一行**，plan+item+date 唯一，提交时双写维护） | plan_id, item_id, record_date, total_times, total_sets, commit_count |

**读写分工（双写模型）**：每次提交在**同一事务**内 1) 事件表 INSERT 一条明细（保留每次提交的"commit 日志"，供「最近提交」/记录历史展示）；2) 汇总表 upsert 当日行（commit_count +1、按模式累加 total_times/total_sets）。聚合查询（进度/剩余/达标/热力图）读汇总表，事件表不参与聚合统计。

> 注：事件表 `training_record` **不能有 (plan_id, item_id, record_date) 唯一约束**（append 模型，同日多条）；汇总表 `training_record_daily` 则**必须有**该唯一约束（一行一天）。事件表索引：`idx_plan_date (plan_id, record_date, create_time)`（详情/进度查询）+ `idx_user_date (user_id, record_date)`（热力图查询）。

枚举：

- `status`：0 已放弃（ABANDONED，预留，暂不自动流转）、1 进行中（IN_PROGRESS）、2 已完成（COMPLETED）、3 已过期（EXPIRED）
- `mode`：`times` 按次数（如 100 个俯卧撑）、`sets` 按次数+组数（如 30 个 × 3 组）
- `unit`：次数/组/公里等，用户自定义展示单位
- `cycleType`：0 不重复（NONE，默认，等同老行为）、1 每天（DAILY）、2 每周（WEEKLY）、3 每月（MONTHLY）、4 每年（YEARLY）
- `cycleAnchor`：周期锚点（int，可空）——每周=星期几（1 周一 ~ 7 周日）；每月=几号（1~31）；每年=月×100+日（如 815 = 8 月 15 日）；空 = 默认锚点（周一 / 1 号 / 1 月 1 日）

**周期（cycleType）语义**：周期计划的目标是「每个周期」的目标——新周期到来时本期进度归零重新累计，但**之前所有完成记录全部保留**（事件表与汇总表不删，热力图/最近提交/记录历史照常展示）。聚合统计（进度/达标/剩余）只读**当前周期**内的每日汇总行；非周期计划（cycleType=0）统计全部记录，行为与历史完全一致。**周期计划永续运行**：不因时间自动流转状态（新周期到了计划仍处于「进行中」，继续按新周期打卡），结束方式只有用户放弃（软删除）或物理删除。

## 接口清单

统一返回结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1755331200000
}
```

---

### 1. GET /train —— 训练首页聚合

返回：近期训练计划（含进度）+ 今年训练热力图。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "plans": [
      {
        "id": 1,
        "title": "居家健身挑战",
        "description": "",
        "startDate": "2026-08-10",
        "endDate": "2026-08-30",
        "status": 1,
        "cycleType": 0,
        "cycleAnchor": null,
        "periodStart": null,
        "periodEnd": null,
        "progress": 55,
        "items": [
          {
            "id": 1,
            "name": "俯卧撑",
            "mode": "times",
            "totalTimes": 100,
            "totalSets": null,
            "unit": "个",
            "doneValue": 55,
            "remainValue": 45,
            "done": false
          }
        ]
      }
    ],
    "heatmap": {
      "year": 2026,
      "totalCount": 2,
      "days": [
        { "date": "2026-08-12", "count": 1 },
        { "date": "2026-08-13", "count": 1 }
      ]
    }
  },
  "timestamp": 1755331200000
}
```

---

### 2. POST /train/plans —— 制定训练计划

请求体（`PlanCreateDTO`）：

```json
{
  "title": "居家健身挑战",
  "description": "坚持每天锻炼",
  "startDate": "2026-08-10",
  "endDate": "2026-08-30",
  "cycleType": 2,
  "cycleAnchor": 3,
  "items": [
    { "name": "俯卧撑", "mode": "times", "totalTimes": 100, "totalSets": null, "unit": "个" },
    { "name": "深蹲", "mode": "sets", "totalTimes": 30, "totalSets": 3, "unit": "个" }
  ]
}
```

校验规则：`title` 非空；`startDate`/`endDate` 非空且开始不晚于结束；`items` 至少一项；项内 `name`/`mode`/`totalTimes` 非空；`totalSets` 仅 sets 模式必填；`cycleType` 缺省为 0（不重复），取值 0~4；`cycleAnchor` 缺省为 null（默认锚点），按周期类型取值——每周 1~7、每月 1~31、每年 月×100+日（日部分 1~31）、每天与不重复必须为空。

响应：`data` 为新计划 id。

```json
{ "code": 200, "message": "操作成功", "data": 4, "timestamp": 1755331200000 }
```

---

### 3. GET /train/plans —— 计划列表（含进度）

返回当前用户（固定 user_id=1）全部训练计划，按创建时间倒序；每个计划含总进度与各训练项完成度/剩余量。未查询到计划时返回空数组。

请求：无参数。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 2,
      "title": "周末拉练计划",
      "description": "",
      "startDate": "2026-08-15",
      "endDate": "2026-08-25",
      "status": 1,
      "cycleType": 0,
      "cycleAnchor": null,
      "periodStart": null,
      "periodEnd": null,
      "progress": 0,
      "items": [
        {
          "id": 3,
          "name": "山野徒步",
          "mode": "times",
          "totalTimes": 60,
          "totalSets": null,
          "unit": "公里",
          "doneValue": 0,
          "remainValue": 60,
          "done": false
        }
      ]
    }
  ],
  "timestamp": 1755331200000
}
```

说明：`items` 字段结构与 `GET /train` 中一致（含 `doneValue`/`remainValue`/`done`），`progress` 为该计划总进度（0~100）。`records` 为该计划全部提交记录（按日期升序、同日按 `createTime` 升序，未提交的天不返回），结构同详情接口；`GET /train` 聚合中的 `plans` 亦含 `records`，前端可一次拉取全量记录，无需逐个查询详情。

---

### 4. GET /train/plans/{id} —— 计划详情

在计划 VO 基础上增加每日提交记录：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "居家健身挑战",
    "description": "",
    "startDate": "2026-08-10",
    "endDate": "2026-08-30",
    "status": 1,
    "cycleType": 2,
    "cycleAnchor": 1,
    "periodStart": "2026-08-10",
    "periodEnd": "2026-08-16",
    "progress": 55,
    "items": [],
    "records": [
      { "id": 21, "date": "2026-08-12", "itemId": 1, "completedSets": 0, "completedTimes": 30, "createTime": "2026-08-12T21:05:12", "updateTime": null },
      { "id": 22, "date": "2026-08-14", "itemId": 2, "completedSets": 1, "completedTimes": 30, "createTime": "2026-08-14T19:47:03", "updateTime": "2026-08-14T20:01:00" }
    ]
  },
  "timestamp": 1755331200000
}
```

`records` 字段说明：

- 按日期升序返回；同日多条按 `createTime` 升序（提交先后）
- 每条含 `id`（提交记录主键，供前端编辑定位）、`createTime`（本次提交时间，ISO 格式 `yyyy-MM-ddTHH:mm:ss`，精确到秒）、`updateTime`（最近编辑时间，未编辑过为 null）。每次提交都追加一条新记录，同一天同一训练项可有多条——前端「最近提交」按 `updateTime ?? createTime` 从新到旧取最近 N 条（编辑过的记录会排到最前）
- 热力图按「记录条数」统计：同一天提交 N 次就是 N 条记录，`totalCount` 等于当年真实提交次数

---

### 5. POST /train/records —— 提交训练项当天完成情况

请求体（`RecordCreateDTO`，全部字段在 body，不用路径参数）：

```json
{
  "planId": 1,
  "itemId": 1,
  "recordDate": "2026-08-16",
  "completedSets": 0,
  "completedTimes": 25
}
```

规则：

- `times` 模式：`completedTimes` = 当天完成总次数（缺省记 0），`completedSets` 忽略（记 0）
- `sets` 模式：`completedSets` = 当天完成组数（缺省记 0）；`completedTimes` = 每组次数，**不传则默认取计划项的 `totalTimes`**
- **每次提交追加一条事件记录**（append）：同一天同一训练项可提交多次，每次都是一条独立的 `training_record`，各自记录提交时间；同时双写更新汇总表当日行（commit_count+1、按模式累加 total_times/total_sets）——完成度 = 汇总表该训练项各行之和

响应：无数据。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

---

### 6. GET /train/heatmap?year=2026 —— 训练热力图

`year` 缺省为今年。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "year": 2026,
    "totalCount": 2,
    "days": [
      { "date": "2026-08-09", "count": 1 },
      { "date": "2026-08-12", "count": 1 }
    ]
  },
  "timestamp": 1755331200000
}
```

---

### 7. POST /train/plans/update —— 编辑计划（含训练项）

请求体（`PlanUpdateDTO`，全部字段在 body，不用路径参数）：

```json
{
  "id": 1,
  "title": "居家健身挑战",
  "description": "坚持每天锻炼",
  "startDate": "2026-08-10",
  "endDate": "2026-08-30",
  "cycleType": 2,
  "cycleAnchor": 3,
  "items": [
    { "id": 1, "name": "俯卧撑", "mode": "times", "totalTimes": 100, "totalSets": null, "unit": "个" },
    { "id": null, "name": "深蹲", "mode": "sets", "totalTimes": 30, "totalSets": 3, "unit": "个" }
  ]
}
```

规则：

- 计划级字段（title/description/startDate/endDate/cycleType/cycleAnchor）整体覆盖更新，并刷新 `update_time`
- `items` 为**整表替换**：`id` 非空 → 更新对应训练项（名称/模式/目标/单位，`sort` 按数组下标重排）；`id` 为空 → 新增训练项；**DB 中存在但请求未提交的训练项 → 删除**（连同其事件表 `training_record` 与汇总表 `training_record_daily` 一并级联删除，保证无孤儿记录）
- 校验规则同「制定计划」；`id` 对应的计划不存在时返回 `code 400`
- **状态保持原值**：编辑不重置计划状态（已放弃/已完成/已过期保持不变）；仅当计划当前为「进行中」时按新数据重判进度与状态流转

响应：无数据。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

---

### 8. POST /train/plans/abandon —— 放弃计划（状态置为已放弃）

请求体（`PlanAbandonDTO`）：

```json
{ "id": 1 }
```

规则：

- 将计划 `status` 置为 `0 已放弃`，并刷新 `update_time`
- **状态机限制**：仅「进行中」的计划可以放弃；已完成/已过期/已放弃的计划调用返回 `code 400`（提示"仅进行中的计划可以放弃"）
- 放弃保留计划及其全部历史打卡与热力图数据（非物理删除）

响应：无数据。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

---

### 9. POST /train/plans/delete —— 物理删除计划（不可恢复）

请求体（`PlanDeleteDTO`）：

```json
{ "id": 1 }
```

规则：

- **数据库层面整体删除**：级联清理事件表 `training_record`、每日汇总表 `training_record_daily`、训练项 `training_plan_item` 与计划本体 `training_plan`，同一事务保证原子性
- 删除后该计划及其全部打卡历史、热力图贡献一并消失，**不可恢复**
- 计划不存在时返回 `code 400`

响应：无数据。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

---

### 10. POST /train/records/update —— 编辑单条打卡记录（仅完成量）

请求体（`RecordUpdateDTO`）：

```json
{
  "id": 21,
  "completedSets": 0,
  "completedTimes": 40
}
```

规则：

- 仅允许修改**完成量**，不允许改 `planId`/`itemId`/`recordDate`（以事件表既有记录为准）
- 模式语义同提交：`times` 模式 `completedTimes`=本次完成次数（`completedSets` 忽略记 0）；`sets` 模式 `completedSets`=本次完成组数、`completedTimes`=每组次数（不传取计划项默认值）
- **事务内**：1) 事件表更新该条 `completed_sets`/`completed_times` 并刷新 `update_time`（提交次数 `commit_count` 不变）；2) 汇总表按「新旧差值」调整当日 `total_times`/`total_sets`（`commit_count` 不变）并刷新 `update_time`；3) 重判计划状态
- 记录 id 不存在时返回 `code 400`

响应：无数据。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

---

## 业务规则

- **热力图 count**：读汇总表按天累加 `commit_count`（当天提交次数）；未提交的天不返回。
- **热力图 totalCount**：该年份提交总次数 = 当年各行 `commit_count` 之和。每次打卡 +1（同日多次提交会累加）。
- **训练项完成度**：`doneValue` —— times 模式 = 汇总表累计 `total_times`；sets 模式 = 累计 `total_sets`。达标 = `doneValue >= 目标值`（times 目标 = `totalTimes`，sets 目标 = `totalSets`）。
- **训练项剩余任务量**：`remainValue = max(0, 目标值 - doneValue)`（已达标/超额为 0）；times 模式单位=次数，sets 模式单位=组数。前端打卡输入框的可用上限即此值。
- **计划进度**：`progress = Σ各训练项完成值 / Σ各训练项目标值 × 100`，向上取整截断，封顶 100。周期计划只统计**当前周期**内的完成值（见下）。
- **周期（cycleType）**：周期计划按自然日历对齐计算「当前周期」`[periodStart, periodEnd]`（与计划开始日期无关）——每天=[当天,当天]；每周=从最近一个锚点星期（缺省周一）到其后第 6 天；每月=本月锚点日（缺省 1 号，超月长取月末）到下月锚点日前一天；每年=本年锚点月日（缺省 1 月 1 日）到次年同日减一天。聚合统计只取当前周期内的每日汇总行；**周期之外的记录全部保留**，仅不参与本期进度/达标/剩余计算。VO 返回 `periodStart`/`periodEnd`（非周期计划为 null）供前端展示「本期」。
- **状态机**：`0 已放弃` 只能由「进行中」经用户**放弃**操作进入（已完成/已过期/已放弃拒绝）；`1 进行中 → 2 已完成/3 已过期` 仅当计划处于"进行中"时自动判定——**非周期计划**：所有训练项达标 → `COMPLETED`，否则当天已超过 `endDate` → `EXPIRED`；**周期计划**：**不因时间自动流转**——新周期自动重置本期进度，计划始终处于「进行中」（历史遗留的 已完成/已过期 周期计划读取时自动恢复为 进行中；已放弃不恢复，为终态）。判定时机：查询（列表/详情）与提交记录后。**物理删除**（`plans/delete`）不属于状态流转，从数据库整体移除。
- **错误返回**：参数校验失败、日期格式错误、计划/训练项不存在等统一返回 `code 400`；未预期异常 `code 500`。

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-08-16 | v1 定稿：GET/POST 两个方法；body 传参不使用路径+body 混搭；去掉 /train/home，聚合直接放 /train；热力图改为按记录条数统计 |
| 2026-08-16 | 领域层重构：创建/提交/进度/状态流转收敛到聚合根；提交字段保持宽松缺省（times 缺省记 0、sets 组数缺省记 0、每组次数缺省取计划值） |
| 2026-08-16 | 热力图新增 `totalCount` 字段：年份提交总次数（全年每天记录条数之和） |
| 2026-08-16 | 训练项新增 `remainValue` 字段：剩余任务量（目标值-已完成值，已达标为 0） |
| 2026-08-16 | 提交语义修正：同一天同一训练项**重复提交改为累加**（每次提交量 = 本次完成量，不覆盖当天已提交值）；原「重复提交走覆盖更新」废弃 |
| 2026-08-16 | 每日记录新增 `updateTime` 字段（最近提交时间，ISO 到秒）；records 同日多条按 `updateTime` 升序，前端「最近提交」按 `updateTime` 从新到旧 |
| 2026-08-16 | **记录模型改为 append（事件流）**：每次提交追加一条 `training_record`，不再按 plan+item+date 查找合并（`mergeSubmit`/`findByPlanItemDate` 废弃）；同日可多条，记录字段 `updateTime` 更名为 `createTime`（本次提交时间）；完成度 = 该训练项所有记录之和（与原累加结果一致）；热力图 `totalCount` = 当年真实提交次数，每次打卡 +1；**schema：删除唯一索引 `uk_plan_item_date`，历史记录清空** |
| 2026-08-16 | **新增每日汇总表 `training_record_daily`（双写模型）**：每训练项每天一行（plan+item+date 唯一），提交事务内 1) 事件表 INSERT 明细 + 2) 汇总表 upsert（commit_count+1、按模式累加 total_times/total_sets）；聚合查询（进度/剩余/达标/热力图）改读汇总表，事件表保留提交明细供「最近提交」；`submitRecord` 加 `@Transactional`；事件表补 `idx_plan_date (plan_id, record_date, create_time)` + 全列 NOT NULL |
| 2026-08-16 | 记录 VO 新增 `id`（记录主键，编辑定位）与 `updateTime`（最近编辑时间，未编辑为 null）；「最近提交」按 `updateTime ?? createTime` 从新到旧 |
| 2026-08-16 | 新增三个接口：`POST /train/plans/update`（编辑计划含训练项，整表替换 + 删除训练项级联删其事件/汇总记录）、`POST /train/plans/abandon`（软删除置 status=0）、`POST /train/records/update`（编辑记录仅完成量，事件表刷新 update_time + 汇总表按差值调整） |
| 2026-08-16 | **状态机调整 + 物理删除**：`abandon` 改为仅「进行中」可放弃（已完成/已过期/已放弃拒绝）；新增 `POST /train/plans/delete` **物理删除**（级联清理事件表/汇总表/训练项/计划本体，事务原子，不可恢复），用于前端"删除"按钮 |
| 2026-08-16 | **新增计划周期（cycleType/cycleAnchor）**：不重复/每天/每周/每月/每年，每周支持锚点星期（1 周一~7 周日，如 每周3=每周三重置），每月/每年预留锚点（日 / 月×100+日）；进度/达标/剩余只统计当前周期内的汇总行（新周期自动归零重计），周期外记录全部保留（热力图/最近提交/记录历史不变）；周期计划不因周期内达标自动转已完成，仅超期转已过期；`training_plan` 新增 `cycle_type`/`cycle_anchor` 两列，VO 新增 `cycleType`/`cycleAnchor`/`periodStart`/`periodEnd` |
| 2026-08-17 | **周期计划改为永续进行中**：周期计划不因时间自动流转状态（不再超 `endDate` 转已过期，也不转已完成），新周期自动重置本期进度，计划始终「进行中」，结束方式仅放弃/物理删除；历史遗留的已完成/已过期周期计划读取时自动恢复为进行中（已放弃为终态不恢复） |
