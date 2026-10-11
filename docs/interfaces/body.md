# 身体数据模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

身体数据模块用于徒步用户的**日常身体状态记录**：每天记录体重与晨起静息心率（可选附一句状态备注），形成长期趋势。训练打卡是「输入」，徒步轨迹是「输出」，身体数据是连接两者的「效果验证」——静息心率长期下降是体能提升最直观的日常信号。

约束（与训练模块一致）：

- 需登录，`/body` 接口从服务端 `HttpSession` 取当前用户 id（`LoginUserInterceptor` 写入 `LoginUserContext`），未登录返回 401
- 只用 `GET` / `POST` 两种方法
- 所有响应统一 `Result<T>` 格式
- 前端只允许调用后端已实现的接口

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `body_metric` | 每日身体数据（**每用户每天一行**，覆盖式更新，非事件流） | user_id, record_date, weight_kg, resting_heart_rate, note |

**与训练记录的模型差异（为什么不用 append 事件流）**：训练打卡「同日多次提交」各自独立（组1、组2…是不同事件），必须事件流；身体数据「同日多次称重」语义上是**对当天 Measurement 的修正**——今天体重 65.2 后又称出 65.0，用户期望的是"今天体重 65.0"，而不是两条记录。因此聚合边界为「用户 × 日期」唯一一条，重复记录当日 = 更新（upsert），`update_time` 记录最近修改时间。

唯一约束：`uk_user_date (user_id, record_date)` —— 应用层先查后写，并发下由 DB 唯一索引兜底（与 user 模块 username/email 唯一策略一致）。

取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `weight_kg` | [25.0, 250.0]，1 位小数 | 成人体重合理区间，超出视为录入错误 |
| `resting_heart_rate` | 可空；非空时 [30, 220] | 次/分，晨起静息测量 |
| `record_date` | ≤ 今天 | 不允许记录未来日期 |
| `note` | ≤ 200 字符 | 睡眠/疲劳/状态等一句话备注 |

## 领域结构（DDD 四层）

```
domain/body
  └── entity/BodyMetric.java               # 聚合根：工厂 record() / 方法 update() 内聚全部不变量校验
  └── repository/BodyMetricRepository.java # 仓储接口：findByUserAndDate / listByUserAndDateRange / save

application/body
  ├── BodyMetricService.java               # 应用服务：upsert 编排 + 趋势/连续天数计算（跨记录统计，不放实体）
  └── cmd/BodyMetricRecordCmd.java         # { date, weightKg, restingHeartRate, note }

infrastructure/body
  ├── mapper/BodyMetricMapper.java         # mybatis-plus BaseMapper
  ├── po/BodyMetricPO.java
  └── repository/impl/BodyMetricRepositoryImpl.java

interfaces/body
  ├── BodyMetricController.java            # /body 路由，只做 HTTP 适配
  ├── converter/BodyMetricConverter.java   # 领域实体 → VO
  ├── dto/command/BodyMetricRecordDTO.java
  └── vo/query/BodyMetricRecordVO.java / BodyMetricSummaryVO.java / BodyMetricRecentVO.java
```

分层说明：

- **不变量（体重/心率区间、日期非未来）**在聚合根工厂与 `update()` 内校验——单条记录自身的规则是实体的事
- **跨记录统计（较上次变化、连续记录天数）**在应用服务计算——涉及多条记录的聚合语义，且实体无状态不应持有
- **并发兜底**：upsert 先查后写，撞 `uk_user_date`（并发首录同一天）捕获 `DuplicateKeyException` 转为更新，保证幂等

## 接口清单

统一返回结构：

```json
{ "code": 200, "message": "操作成功", "data": {}, "timestamp": 1755331200000 }
```

---

### 1. POST /body/record —— 记录/更新当日身体数据

同一日期重复提交 = 覆盖更新（见「数据模型」），响应始终返回落库后的最新记录，前端可据此即时更新图表与列表。

请求体（`BodyMetricRecordDTO`）：

```json
{
  "date": "2026-10-11",
  "weightKg": 65.2,
  "restingHeartRate": 58,
  "note": "昨夜睡足 8 小时"
}
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| date | string | 否 | yyyy-MM-dd；缺省为今天；不允许未来日期 |
| weightKg | number | 是 | 25.0 ~ 250.0 |
| restingHeartRate | int | 否 | 30 ~ 220 |
| note | string | 否 | ≤ 200 字符，trim 后落库 |

响应：`data` 为落库后的记录（含 id 与 updateTime）。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 3,
    "date": "2026-10-11",
    "weightKg": 65.2,
    "restingHeartRate": 58,
    "note": "昨夜睡足 8 小时",
    "updateTime": "2026-10-11T07:30:00"
  },
  "timestamp": 1755331200000
}
```

错误返回：参数校验失败 → `code 400` + 中文 message（如「体重需在 25.0 ~ 250.0 kg 之间」「记录日期不能是未来」）。

---

### 2. GET /body/recent —— 最近 N 天记录 + 汇总

`days` 缺省 30，钳制到 [7, 365]。返回区间内**升序**记录（供折线图直接使用）与汇总统计。

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      { "id": 1, "date": "2026-10-01", "weightKg": 66.0, "restingHeartRate": 60, "note": null, "updateTime": null },
      { "id": 2, "date": "2026-10-05", "weightKg": 65.5, "restingHeartRate": 59, "note": null, "updateTime": null },
      { "id": 3, "date": "2026-10-11", "weightKg": 65.2, "restingHeartRate": 58, "note": "昨夜睡足 8 小时", "updateTime": "2026-10-11T07:30:00" }
    ],
    "summary": {
      "latestWeight": 65.2,
      "prevWeight": 65.5,
      "weightDelta": -0.3,
      "recordedDays": 3,
      "streakDays": 2
    }
  },
  "timestamp": 1755331200000
}
```

`summary` 字段说明：

| 字段 | 说明 |
|---|---|
| latestWeight | 最近一条记录的体重；无记录时 null |
| prevWeight | 倒数第二条记录的体重；不足两条时 null |
| weightDelta | latest - prev，1 位小数；不足两条时 null。负数=下降 |
| recordedDays | 查询区间内有记录的天数 |
| streakDays | 连续记录天数：从今天往回数连续有记录的天数；今天未记录则从昨天起算（保护"晚上才称重"的连续性） |

区间定义：以今天为终点向前取 `days` 天（含今天）。区间外的历史记录不返回（前端图表取 30/90 天窗口足够）。

---

## 业务规则

- **每日一条**：同日重复提交覆盖更新（体重/心率/备注全量替换），`update_time` 刷新；`create_time` 保持首次记录时间
- **趋势图数据**：前端直接用 `records`（升序）绘制折线；只有 1 条记录时不画线只画点
- **删除**：暂不提供（学习项目，删除接口待需要时再加；物理删除会破坏趋势连续性，需慎重）
- **错误返回**：参数校验失败统一 `code 400`；未预期异常 `code 500`

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /body/record（每日一条 upsert）+ GET /body/recent（近 N 天记录 + 汇总）；体重/静息心率/备注；uk_user_date 唯一索引兜底并发 |
