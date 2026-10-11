# 路线轨迹模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码（v1.1 增补 /routes/hot）

## 概述

路线轨迹模块承载徒步的**客观输出**：上传 GPX / KML 轨迹文件，后端解析轨迹点并计算里程（Haversine）、时长（首尾时间差）、累计爬升（相邻点高程正向差），地图回放（MapLibre + 天地图瓦片）。

约束：需登录、统一 `Result<T>`、当前用户 id 从登录会话上下文取。

## 数据模型

`route_track`：轨迹点 JSON（MEDIUMTEXT）与途径点 JSON 同表存储；`distance` / `duration_min` / `elevation_gain` 由领域层在 `create()` 时计算并持久化。

## 领域结构（DDD 四层）

```
domain/route
  └── entity/RouteTrack.java               # 聚合根：工厂 create()（校验+计算）/ reconstruct()
  └── repository/RouteTrackRepository.java # save / findById / listByUserId / delete

application/route
  ├── RouteService.java                    # 编排
  ├── RouteHotService.java                 # v1.1：常走路线聚合（跨模块只读 journal 计数）
  └── cmd/RouteCreateCmd.java

infrastructure/route
  ├── mapper/RouteTrackMapper.java
  ├── po/RouteTrackPO.java                 # pointsJson / waypointsJson 走 Jackson ObjectMapper
  └── repository/impl/RouteTrackRepositoryImpl.java

interfaces/route
  ├── RouteController.java
  ├── converter/RouteConverter.java
  ├── dto/command/RouteCreateDTO.java
  └── vo/query/RouteTrackVO.java / RouteTrackDetailVO.java / RouteHotVO.java (v1.1)
```

## 接口清单

### 1. POST /routes —— 上传轨迹

请求体：`{ name, description?, points: [{lng,lat,ele?,time?}], waypoints?: [{lng,lat,ele?,name}] }`。
领域工厂 `create()` 校验点数（≥2）、经纬度范围，并计算三项指标。响应：新轨迹 id。

### 2. GET /routes —— 我的全部轨迹（createTime 倒序）

### 3. GET /routes/{id} —— 轨迹详情（含全部轨迹点/途径点）

### 4. DELETE /routes/{id} —— 删除轨迹（物理删除）

### 5. GET /routes/hot —— 常走路线（v1.1 增补）

产品语义：单用户系统里「热门」= **我常走的路线**。按 `hike_journal.route_id` 的引用次数聚合排序（走的次数多 = 热），让「哪条线我最常走、最近一次什么时候走」一眼可见，也为「再走一次」的复购式动线服务（详情页一键再写日记）。

```
GET /routes/hot?limit=5
```

`limit` 钳制 [1, 20]，缺省 5。响应（`data` 数组，hikeCount 降序）：

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [{
    "routeId": 3,
    "name": "海坨山南大梁环穿",
    "distanceKm": 18.60,
    "durationMin": 420,
    "elevationGain": 1250.0,
    "hikeCount": 4,
    "lastHikeDate": "2026-10-11",
    "firstHikeDate": "2026-03-14"
  }]
}
```

口径（应用服务 `RouteHotService` 跨模块只读聚合，同成就徽章先例）：

- 数据源 = `HikeJournalRepository.listByUser(userId)` 中 `routeId` 非空的日记 + `RouteTrackRepository.listByUserId(userId)` 的路线信息
- `hikeCount` = 该路线被日记引用次数；`firstHikeDate` / `lastHikeDate` = 最早/最近一次徒步日期
- 排序：hikeCount 降序 → lastHikeDate 降序；只返回 hikeCount ≥ 1 的路线
- 无任何关联日记时返回空数组（前端隐藏区块）

### 业务规则

- 归属过滤同其他模块；删除轨迹后日记的 routeId 悬空（routeName 显示为空，不级联清理）
- 错误返回：参数校验/不存在 400；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿（补录）：POST /routes、GET /routes、GET /routes/{id}、DELETE /routes/{id}；GPX/KML 前端解析、后端算指标 |
| 2026-10-12 | v1.1 增补：GET /routes/hot 常走路线（按日记引用聚合），消化首页「热门路线」死链 |
