# 装备模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

装备是徒步用户投入最大的资产，也是每次出发前的固定仪式。本模块覆盖两个场景：

- **装备库**（gear_item）：我的装备台账——品类、自重（克）、品牌。核心价值是「基础重量（base weight）意识」：知道自己背负系统里每一件东西多重，才谈得上轻量化。
- **打包清单**（gear_checklist + gear_checklist_item）：一次出行的 checklist——从装备库挑选或临时加项，出发前逐项勾选，防止把帐篷忘在家里。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `gear_item` | 装备台账 | user_id, name, category, weight_grams?, brand?, note?, status |
| `gear_checklist` | 打包清单头 | user_id, title, hike_date?, note?, status |
| `gear_checklist_item` | 清单条目 | checklist_id, gear_item_id?, name, weight_grams?, checked, sort |

三张表三个聚合边界：装备库与清单是两个独立聚合；清单条目是清单聚合的内部实体（离开清单无意义）。**条目对装备库是弱引用**：添加时快照名称与克重（`name`/`weight_grams` 落在条目上），之后装备库改名/退役不影响已有清单——清单是历史凭证，不是装备库的视图。

取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `name` | [1, 100] 字符 | 装备名 / 条目名 |
| `category` | [1, 11] | 1 背包 / 2 帐篷天幕 / 3 睡袋防潮垫 / 4 鞋袜 / 5 服装 / 6 炊具饮水 / 7 照明 / 8 导航 / 9 安全急救 / 10 电子设备 / 11 其他 |
| `weight_grams` | 可空；非空 [0, 30000] | 单件克重（背包上限 30kg 足够覆盖） |
| `brand` | ≤ 50 字符 | 可空 |
| `note` | ≤ 200 字符 | 可空，如「40L，主力包」 |
| `status`（item） | 0 已退役 / 1 在役 | 退役不删，保留台账与历史清单对应关系 |
| `hike_date` | 可空 | 清单对应的出行日期 |
| `status`（checklist） | 0 准备中 / 1 已打包 / 2 已结束 | |
| `checked` | 0 / 1 | 条目勾选 |
| `sort` | [0, 9999] | 条目排序 |

数量约束：单个清单条目数 ≤ 100；装备库单用户无上限（个人使用量级）。

索引：`gear_item.idx_user_id`、`gear_checklist.idx_user_id`、`gear_checklist_item.idx_checklist_id`。

## 领域结构（DDD 四层）

```
domain/gear
  ├── entity/GearItem.java                  # 聚合根：工厂 create() / update()
  ├── entity/GearChecklist.java             # 聚合根：持有 List<GearChecklistItem>，addItem/renumber
  ├── entity/GearChecklistItem.java         # 清单内实体
  └── repository/GearItemRepository.java / GearChecklistRepository.java

application/gear
  ├── GearItemService.java                  # 装备库 CRUD 编排
  ├── GearChecklistService.java             # 清单编排：归属校验、条目数上限、复制清单、级联删
  └── cmd/…                                 # GearItemCmd / GearChecklistCmd / ChecklistItemCmd / CopyCmd…

infrastructure/gear
  ├── mapper/GearItemMapper.java / GearChecklistMapper.java / GearChecklistItemMapper.java
  ├── po/GearItemPO.java / GearChecklistPO.java / GearChecklistItemPO.java
  └── repository/impl/GearItemRepositoryImpl.java / GearChecklistRepositoryImpl.java

interfaces/gear
  ├── GearItemController.java / GearChecklistController.java
  ├── converter/GearConverter.java
  ├── dto/command/…（各写接口的 DTO）
  └── vo/query/GearItemVO.java / GearChecklistVO.java（含 items）/ GearChecklistSummaryVO.java
```

分层说明：条目的勾选/改名是清单聚合的内部操作，整体由聚合根维护一致性与 `sort` 连续性；「复制清单」「条目数上限」「清单与条目的归属链校验」在应用服务。

## 接口清单

### 装备库（GearItemController，/gear/items）

| 接口 | 请求体 | 说明 |
|---|---|---|
| GET /gear/items | – | 全量列表，`status` 升序（在役在前）+ `category` 升序 + id 升序 |
| POST /gear/items | `{ name, category, weightGrams?, brand?, note? }` | 新建，status=1 在役 |
| POST /gear/items/update | `{ id, name, category, weightGrams?, brand?, note?, status }` | 全量替换（weightGrams 显式传 null 才清空） |
| POST /gear/items/delete | `{ id }` | 物理删除；**不**级联清理清单条目（条目是快照） |

### 打包清单（GearChecklistController，/gear/checklists）

| 接口 | 请求体 | 说明 |
|---|---|---|
| GET /gear/checklists | – | 清单列表（含全部条目），`status` 升序 + `create_time` 倒序 |
| POST /gear/checklists | `{ title, hikeDate?, note? }` | 新建空清单，status=0 |
| POST /gear/checklists/update | `{ id, title?, hikeDate?, note?, status? }` | 部分更新（非 null 覆盖） |
| POST /gear/checklists/delete | `{ id }` | 删清单**并级联删条目** |
| POST /gear/checklists/copy | `{ id }` | 复制为新清单（标题追加「 · 副本」、status=0、条目全带 checked=0），返回新清单 VO |
| POST /gear/checklists/items/add | `{ checklistId, name, gearItemId?, weightGrams? }` | 加条目（gearItemId 仅为来源标记，可空） |
| POST /gear/checklists/items/update | `{ id, checked?, name?, weightGrams? }` | 部分更新（勾选/改名/改克重） |
| POST /gear/checklists/items/delete | `{ id }` | 删单条 |

响应示例（GET /gear/checklists 的单个元素）：

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [{
    "id": 2, "title": "海坨山两日重装", "hikeDate": "2026-10-18", "note": null,
    "status": 0, "createTime": "2026-10-11T09:00:00",
    "items": [
      { "id": 11, "gearItemId": 3, "name": "三峰 出云 40L", "weightGrams": 1480, "checked": true },
      { "id": 12, "gearItemId": null, "name": "路餐（坚果+能量胶）", "weightGrams": 400, "checked": false }
    ]
  }]
}
```

## 业务规则

- 清单条目的增删改都先校验清单归属（条目 id 反查其 checklist 属主），防越权
- 删除装备库条目不影响清单快照；删除清单级联删条目
- 复制清单在新事务内完成，条目 sort 按原顺序重排
- 错误返回：参数校验 400（含「清单条目已达上限（100）」）；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：装备库 4 接口 + 打包清单 8 接口；条目快照式弱引用装备库；清单复制；级联删除 |
