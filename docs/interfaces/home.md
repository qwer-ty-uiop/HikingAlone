# 首页模块接口设计

## 概述

首页数据聚合接口：导航菜单 + 横幅轮播，一次请求返回首页渲染所需的全部数据。

- 模块定位：`interfaces/index`（Controller）/ `application/index`（Service）/ `domain/index`（领域仓储接口）/ `infrastructure/index`（MyBatis-Plus 实现），DDD 四层按模块分包
- **公开接口**：不在 `LoginUserInterceptor` 拦截清单内，未登录可访问（首页是落地页）
- 数据全部来自两张配置表，无用户维度数据，无写接口

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `nav_menu` | 导航菜单（父子二级，`parent_id=0` 为顶级） | name, link_url, parent_id, sort |
| `home_banner` | 首页横幅（`status` 1 启用 / 0 禁用） | title, image_url, link_url, sort, status |

## 接口清单

### GET /home —— 首页数据

无请求参数，无需登录。

响应 `data`：

| 字段 | 类型 | 说明 |
|---|---|---|
| navMenus | NavMenuVO[] | 全量菜单，按 sort 升序 |
| banners | HomeBannerVO[] | 仅 status=1，按 sort 升序 |

NavMenuVO：`id`、`name`、`linkUrl`、`parentId`（顶级为 0，非 0 表示下拉子菜单的父 id）
HomeBannerVO：`id`、`title`、`imageUrl`、`linkUrl`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "navMenus": [
      { "id": 1, "name": "首页", "linkUrl": "/", "parentId": 0 },
      { "id": 6, "name": "热门路线", "linkUrl": "/routes/hot", "parentId": 2 }
    ],
    "banners": [
      { "id": 1, "title": "秋季山野徒步季", "imageUrl": "https://picsum.photos/seed/banner1/1600/600", "linkUrl": "/routes" }
    ]
  },
  "timestamp": 1755331200000
}
```

业务规则：

- 菜单/横幅内容运营可配（改表数据即生效），前端按 parentId 组装下拉、不写死路由
- linkUrl 均为前端 SPA 路由（如 `/routes`、`/daily`），新增菜单前先确认前端路由存在（2026-10-12 曾做过一轮死链修正）

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-25 | 补建文档（接口先于文档上线，本文档为事后补建）；锁定现有行为：菜单全量 + 横幅仅启用 |
