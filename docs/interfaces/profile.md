# 个人中心（Dashboard）模块接口设计

> 文档版本：v1 · 2026-10-12 · 状态：按此设计编码

## 概述

个人中心（`/profile`）是「一人徒」的**今日驾驶舱**：打开就看到今天的打卡状态、该做什么、走得怎么样——把分散在各模块的数据聚合成一屏，把「要用户手动到处填表」变成「系统提醒 + 一键完成」。

设计借鉴：Keep/Strava 的「今日/概览」首页——最重要的操作（今日打卡）放最顺手的位置，最重要的提醒（该写日记了、目标落后了）主动推给用户。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、**零表设计**（全部读时聚合，同成就/统计先例）。

## 设计决策

- **零表**：Dashboard 是已有数据的另一种读法，无新增持久化
- **跨模块只读聚合**：应用服务依赖 daily/journal/goal/wish/achievement 域的仓储与应用服务，不穿透他域内部表
- **待办（todos）是规则引擎**：服务端根据数据状态计算提醒，前端只渲染——规则集中在一处，便于演进

## 待办规则（v1，两条）

| 规则 | 触发条件 | 文案 | 跳转 |
|---|---|---|---|
| 轨迹未写日记 | 最近 3 天内上传过轨迹，且不存在 `hike_date` ≥ 轨迹上传日当天的日记 | 「有 N 条新轨迹还没写日记」 | /journal |
| 目标进度落后 | 进行中的月度/年度目标，进度百分比 < 日历时间进度（月度：已过天数/当月天数；年度：已过天数/全年天数），且周期未结束 | 「{label}进度落后（{percent}%，时间已过 {calendarPercent}%）」 | /goal |

规则命中即生成一条（同规则不重复）；无待办返回空数组（前端隐藏区块）。

## 领域结构（DDD 四层）

```
application/profile
  └── ProfileService.java                  # 跨模块聚合装配（复用 JournalService.summary 等既有应用服务能力）
  └── cmd/（无——纯读）

interfaces/profile
  ├── ProfileController.java               # GET /profile/dashboard
  ├── converter/ProfileConverter.java
  └── vo/query/DashboardVO.java            # 内嵌 TodayVO / TodoVO / 各摘要结构
```

## 接口清单

### GET /profile/dashboard —— 驾驶舱聚合

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "today": {
      "waterCups": 3, "steps": 8231, "sleepHours": 7.5, "sleepQuality": 4, "checkedIn": true
    },
    "todos": [
      { "type": "JOURNAL_PENDING", "message": "有 1 条新轨迹还没写日记", "target": "/journal" },
      { "type": "GOAL_BEHIND", "message": "月徒步里程进度落后（24%，时间已过 38%）", "target": "/goal" }
    ],
    "hiking": {
      "totalCount": 12, "totalDistanceKm": 186.4, "totalElevationGain": 8320.0,
      "thisYearCount": 8, "thisYearDistanceKm": 121.3
    },
    "streaks": { "journalStreakMonths": 3, "bodyStreakDays": 6 },
    "recentJournals": [
      { "id": 9, "title": "雨后初晴 · 海坨山南大梁", "hikeDate": "2026-10-11", "distanceKm": 18.6 }
    ],
    "activeGoals": [
      { "id": 1, "metricLabel": "月徒步里程", "targetValue": 80, "current": 46.3, "percent": 58, "unit": "km" }
    ],
    "wish": { "wishCount": 2, "plannedCount": 1, "doneCount": 7 },
    "achievement": { "achievedCount": 5, "totalCount": 14 }
  }
}
```

字段口径：

| 字段 | 来源 |
|---|---|
| today | `DailyCheckinRepository.findByUserAndDate(今天)`；无记录 `checkedIn=false`、数值 null |
| todos | 两条规则实时计算（见上） |
| hiking | 复用徒步日记汇总口径（JournalService.summary） |
| streaks.journalStreakMonths | 最近连续有徒步日记的月份数（按 hike_date 的年月，从当月起往回数） |
| streaks.bodyStreakDays | 身体数据连续记录天数（复用 body 模块口径：今天没记从昨天起算） |
| recentJournals | 最近 3 篇（id/title/hikeDate/distanceKm） |
| activeGoals | status=1 的目标（最多 4 个，period 降序），带读时进度（复用 GoalService 口径） |
| wish / achievement | 心愿各状态数量；徽章已达成数/总数 |

## 业务规则

- 只读接口，无写副作用；今日打卡的「一键 +1」走既有 `POST /daily/checkin`（部分更新），Dashboard 不另设写接口
- 任何子模块数据为空都不影响整体响应（各结构给空值/零值）
- 错误返回：未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-12 | v1 定稿：GET /profile/dashboard；零表读时聚合 + 两条待办规则（轨迹未写日记、目标进度落后） |
