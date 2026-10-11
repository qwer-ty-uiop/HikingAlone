# 每日打卡模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

每日打卡模块覆盖徒步用户**日常健康习惯**中最轻量但最该坚持的三件事：**饮水**（徒步是最好的脱水教育，日常就该练）、**步数**（日常活动量的底座）、**睡眠**（恢复质量决定第二天能走多远）。三者都是「每天一个数」，因此合并为一个模块、一张表、一组接口——避免三个同构模块的重复建设。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `daily_checkin` | 每日打卡（**每用户每天一行**，部分字段独立覆盖） | user_id, record_date, water_cups?, steps?, sleep_hours?, sleep_quality? |

**部分更新语义**（与 body 模块整行覆盖的差异）：身体数据是「一次称重得到一组数」，同日再称是修正；每日打卡是**一天中多次、各自独立**的行为——早上记睡眠、中午 +1 杯水、晚上记步数，互不相干。因此 `POST /daily/checkin` 只更新请求中**非 null** 的字段，同表其他字段保持不变。整行首次创建与后续部分更新共用 upsert。

字段取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `water_cups` | [0, 30] | 杯（按 250ml/杯计）；0 表示刻意记录「今天没喝够」 |
| `steps` | [0, 200000] | 步，整数 |
| `sleep_hours` | (0, 24]，1 位小数 | 小时 |
| `sleep_quality` | [1, 5] | 主观评分 1 很差 ~ 5 很好 |
| `record_date` | ≤ 今天 | 不允许补未来的数 |

唯一约束：`uk_user_date (user_id, record_date)`，应用层先查后写 + `DuplicateKeyException` 兜底转更新（与 body 模块一致）。

## 领域结构（DDD 四层）

```
domain/daily
  └── entity/DailyCheckin.java               # 聚合根：工厂 record()（全量新建）/ merge()（部分覆盖）
  └── repository/DailyCheckinRepository.java # findByUserAndDate / listByUserAndDateRange / save

application/daily
  ├── DailyCheckinService.java               # upsert 编排 + 7 天均值 / 记录天数统计
  └── cmd/DailyCheckinCmd.java               # { date?, waterCups?, steps?, sleepHours?, sleepQuality? }

infrastructure/daily
  ├── mapper/DailyCheckinMapper.java
  ├── po/DailyCheckinPO.java
  └── repository/impl/DailyCheckinRepositoryImpl.java

interfaces/daily
  ├── DailyCheckinController.java
  ├── converter/DailyCheckinConverter.java
  ├── dto/command/DailyCheckinDTO.java
  └── vo/query/DailyCheckinVO.java / DailyRecentVO.java / DailySummaryVO.java
```

分层说明：区间校验与部分合并规则（merge 只覆盖非 null 字段）在聚合根；跨记录统计（均值、记录天数）在应用服务。

## 接口清单

### 1. POST /daily/checkin —— 打卡（部分更新）

请求体（至少一个指标字段非 null，否则 400「请至少填写一项打卡数据」）：

```json
{ "date": "2026-10-11", "waterCups": 3 }
```

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| date | string | 否 | yyyy-MM-dd，缺省今天；不允许未来 |
| waterCups | int | 否 | 0~30，非 null 才覆盖 |
| steps | int | 否 | 0~200000，非 null 才覆盖 |
| sleepHours | number | 否 | (0, 24]，非 null 才覆盖 |
| sleepQuality | int | 否 | 1~5，非 null 才覆盖 |

响应：当天落库后的完整记录（未填字段为 null）。

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": { "id": 5, "date": "2026-10-11", "waterCups": 3, "steps": null,
            "sleepHours": 7.5, "sleepQuality": 4, "updateTime": "2026-10-11T12:30:00" }
}
```

### 2. GET /daily/recent?days=30 —— 最近记录 + 汇总

`days` 钳制 [7, 365]，区间含今天，记录升序返回（图表直接可用）。

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "records": [
      { "id": 4, "date": "2026-10-10", "waterCups": 6, "steps": 8231, "sleepHours": 7.0, "sleepQuality": 3, "updateTime": null },
      { "id": 5, "date": "2026-10-11", "waterCups": 3, "steps": null, "sleepHours": 7.5, "sleepQuality": 4, "updateTime": "2026-10-11T12:30:00" }
    ],
    "summary": {
      "recordedDays": 2,
      "avgWater7": 4.5,
      "avgSteps7": 8231.0,
      "avgSleep7": 7.3
    }
  }
}
```

`summary` 说明：`recordedDays` 为区间内有任何记录的天数；三个 7 天均值为**各自有值的天数**的平均（分母不含 null 天），无值时为 null。

## 业务规则

- 部分更新只覆盖非 null 字段，`update_time` 任一次写入都刷新
- 删除暂不提供（与身体数据模块同理由）；记录错了直接重新打卡覆盖
- 错误返回：参数校验 400；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /daily/checkin（部分字段覆盖 upsert）+ GET /daily/recent（升序记录 + 7 天均值）；饮水/步数/睡眠合并在 daily_checkin 一表 |
