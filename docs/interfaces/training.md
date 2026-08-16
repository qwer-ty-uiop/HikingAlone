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
| `training_plan` | 训练计划 | title, description, start_date, end_date, status |
| `training_plan_item` | 计划训练项（一个计划多个项） | name, mode(times/sets), total_times, total_sets, unit, sort |
| `training_record` | 训练项当天完成记录 | plan_id, item_id, record_date, completed_sets, completed_times |

枚举：

- `status`：0 已放弃（ABANDONED，预留，暂不自动流转）、1 进行中（IN_PROGRESS）、2 已完成（COMPLETED）、3 已过期（EXPIRED）
- `mode`：`times` 按次数（如 100 个俯卧撑）、`sets` 按次数+组数（如 30 个 × 3 组）
- `unit`：次数/组/公里等，用户自定义展示单位

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
  "items": [
    { "name": "俯卧撑", "mode": "times", "totalTimes": 100, "totalSets": null, "unit": "个" },
    { "name": "深蹲", "mode": "sets", "totalTimes": 30, "totalSets": 3, "unit": "个" }
  ]
}
```

校验规则：`title` 非空；`startDate`/`endDate` 非空且开始不晚于结束；`items` 至少一项；项内 `name`/`mode`/`totalTimes` 非空；`totalSets` 仅 sets 模式必填。

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

说明：`items` 字段结构与 `GET /train` 中一致（含 `doneValue`/`remainValue`/`done`），`progress` 为该计划总进度（0~100）。`records` 为该计划全部每日提交记录（按日期升序，未提交的天不返回），结构同详情接口；`GET /train` 聚合中的 `plans` 亦含 `records`，前端可一次拉取全量记录，无需逐个查询详情。

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
    "progress": 55,
    "items": [],
    "records": [
      { "date": "2026-08-12", "itemId": 1, "completedSets": 0, "completedTimes": 30 },
      { "date": "2026-08-14", "itemId": 2, "completedSets": 1, "completedTimes": 30 }
    ]
  },
  "timestamp": 1755331200000
}
```

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
- 同一天对同一训练项重复提交 = **累加**（每次提交的是本次完成量，与当天已提交值相加，按 plan+item+date 唯一）

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

## 业务规则

- **热力图 count**：按用户+年份统计每天 `training_record` 的**记录条数**（即当天提交了几次，不是完成的次数/组数总和）；未提交的天不返回。
- **热力图 totalCount**：该年份提交总次数，即全年每天 `count` 之和（等价于该用户该年度的记录总条数）。
- **训练项完成度**：`doneValue` —— times 模式 = 累计 `completedTimes`；sets 模式 = 累计 `completedSets`。达标 = `doneValue >= 目标值`（times 目标 = `totalTimes`，sets 目标 = `totalSets`）。
- **训练项剩余任务量**：`remainValue = max(0, 目标值 - doneValue)`（已达标/超额为 0）；times 模式单位=次数，sets 模式单位=组数。前端打卡输入框的可用上限即此值。
- **计划进度**：`progress = Σ各训练项完成值 / Σ各训练项目标值 × 100`，向上取整截断，封顶 100。
- **状态流转**：仅当计划处于"进行中"时自动判定——所有训练项达标 → `COMPLETED`；否则当天已超过 `endDate` → `EXPIRED`。判定时机：查询（列表/详情）与提交记录后。
- **错误返回**：参数校验失败、日期格式错误、计划/训练项不存在等统一返回 `code 400`；未预期异常 `code 500`。

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-08-16 | v1 定稿：GET/POST 两个方法；body 传参不使用路径+body 混搭；去掉 /train/home，聚合直接放 /train；热力图改为按记录条数统计 |
| 2026-08-16 | 领域层重构：创建/提交/进度/状态流转收敛到聚合根；提交字段保持宽松缺省（times 缺省记 0、sets 组数缺省记 0、每组次数缺省取计划值） |
| 2026-08-16 | 热力图新增 `totalCount` 字段：年份提交总次数（全年每天记录条数之和） |
| 2026-08-16 | 训练项新增 `remainValue` 字段：剩余任务量（目标值-已完成值，已达标为 0） |
| 2026-08-16 | 提交语义修正：同一天同一训练项**重复提交改为累加**（每次提交量 = 本次完成量，不覆盖当天已提交值）；原「重复提交走覆盖更新」废弃 |
