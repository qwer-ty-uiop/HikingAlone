# 成就徽章模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

成就徽章是对全站数据的**游戏化读法**：不新增任何用户输入，从徒步日记、训练打卡、身体数据三个已产生的数据流中提炼里程碑，给坚持一个可看见的形状。徽章目录由代码定义（不是数据），服务端按当前数据实时计算「已达成/进行中 + 进度」。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`。

## 设计决策

- **零表设计**：徽章不落库。达成与否随时可由数据重算（数据被删徽章自然回落），避免「发出去的徽章与数据不一致」的对账问题；将来若要永久铭牌（首徒日期、达成时刻），再引入落库表
- **跨模块聚合**：应用服务依赖 journal / training / body 三个域的仓储接口做只读聚合，不穿透到各域内部表
- **目录即代码**：徽章定义（分组、名称、阈值、描述）放在领域层 `AchievementCatalog`，新增徽章 = 加一行目录，无需迁移

## 徽章目录（v1，共 14 枚）

| 分组 | 徽章 | 达成条件 |
|---|---|---|
| 步履（徒步日记） | 初次出发 | 累计徒步 ≥ 1 次 |
| 步履 | 渐入佳境 | 累计徒步 ≥ 10 次 |
| 步履 | 山野常客 | 累计徒步 ≥ 50 次 |
| 里程（徒步日记） | 迈出第一步 | 累计里程 ≥ 10 km |
| 里程 | 百里行人 | 累计里程 ≥ 100 km |
| 里程 | 千里江山 | 累计里程 ≥ 500 km |
| 爬升（徒步日记） | 越过小丘 | 累计爬升 ≥ 500 m |
| 爬升 | 层峦叠嶂 | 累计爬升 ≥ 5000 m |
| 爬升 | 云海之巅 | 累计爬升 ≥ 20000 m |
| 单日（徒步日记） | 半程马拉松 | 单日里程 ≥ 21.1 km |
| 单日 | 全程马拉松 | 单日里程 ≥ 42.2 km |
| 自律（训练打卡） | 热身完毕 | 累计训练打卡 ≥ 10 次 |
| 自律 | 自律成瘾 | 累计训练打卡 ≥ 100 次 |
| 自律 | 钢铁意志 | 累计训练打卡 ≥ 500 次 |

## 领域结构（DDD 四层）

```
domain/achievement
  ├── entity/Achievement.java              # 值对象：code/group/name/description/current/target/achieved
  └── AchievementCatalog.java              # 静态目录：徽章定义 + 各分组数据提取口径

application/achievement
  └── AchievementService.java              # 依赖 journal/training/body 仓储聚合 current 值，套目录产出 VO

interfaces/achievement
  ├── AchievementController.java
  ├── converter/AchievementConverter.java
  └── vo/query/AchievementVO.java / AchievementGroupVO.java
```

数据口径：

- 步履/里程/爬升/单日：`HikeJournalRepository.listByUser(userId)` 聚合（count、Σdistance、Σelevation、max(distance)）
- 自律：训练打卡次数取 `training_record_daily` 的 commit_count 总和（复用训练域已有仓储能力，必要时为其补只读方法）

## 接口清单

### GET /achievement/all —— 徽章全量（按分组）

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [
    {
      "group": "步履",
      "achievements": [
        { "code": "hike_count_1", "name": "初次出发", "description": "完成第一次徒步", "achieved": true, "current": 12, "target": 1, "progressPercent": 100 },
        { "code": "hike_count_10", "name": "渐入佳境", "description": "累计徒步 10 次", "achieved": true, "current": 12, "target": 10, "progressPercent": 100 },
        { "code": "hike_count_50", "name": "山野常客", "description": "累计徒步 50 次", "achieved": false, "current": 12, "target": 50, "progressPercent": 24 }
      ]
    }
  ]
}
```

`progressPercent` = min(100, floor(current/target*100))，target 为 0 的情形不存在（目录保证阈值 > 0）。

## 业务规则

- 只读接口，无任何写副作用
- 数据为空时全部徽章 achieved=false、progressPercent=0（前端渲染「未解锁」态）
- 错误返回：未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：GET /achievement/all；零表、目录即代码、跨域只读聚合；14 枚徽章分 4 组 |
