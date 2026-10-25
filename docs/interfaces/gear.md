# 装备模块接口设计

> 文档版本：v1.2 · 2026-10-13 · 状态：按此设计编码（v1.2 增补常用装备目录与可视化装包）

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

## AI 装备建议（v1.1 增补）

### 设计决策

- **LLM 供应商无关**：走 OpenAI 兼容 chat/completions 协议（DeepSeek / 通义千问 / 智谱 / 自建 vLLM 均兼容），base-url、api-key、model 全部配置化（环境变量注入），换供应商零代码改动
- **优雅降级**：未配置 `LLM_API_KEY` 或调用失败时，自动回落内置**规则引擎**（按行程参数推导），响应 `source` 字段区分 `LLM` / `RULE`，前端透明展示——功能永远可用，配了 key 更聪明
- **上下文注入**：建议时自动带入关联路线（名称/里程/爬升/时长）与用户装备库现有清单，让 LLM 只推荐「缺的、该带的」，不推已知装备
- **DDD 依赖倒置**：`domain/gear/service/GearAdvisor` 领域接口 + `infrastructure/llm/OpenAiCompatGearAdvisor` 实现，应用服务只依赖接口

### POST /gear/suggest —— 生成装备建议

请求体：

```json
{
  "routeId": 3,
  "days": 2,
  "distanceKm": null,
  "month": 11,
  "extra": "可能有积雪，想带狗"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| routeId | Long | 否 | 关联路线：自动带出名称/里程/爬升/时长（须属于当前用户；传入 distanceKm 显式覆盖里程） |
| days | int | 否 | 行程天数，钳制 [1, 7]，缺省 1 |
| distanceKm | BigDecimal | 否 | 显式里程（无 routeId 时建议提供），(0, 500] |
| month | int | 否 | 出行月份，钳制 [1, 12]，缺省当前月 |
| extra | string | 否 | 自由补充（天气/同伴/宠物等，≤200 字） |

响应：

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": {
    "source": "LLM",
    "model": "deepseek-chat",
    "items": [
      { "name": "冰爪（简易四齿）", "category": 4, "weightGrams": 400, "priority": 1, "reason": "11 月高海拔可能有积雪结冰" },
      { "name": "抓绒中间层", "category": 5, "weightGrams": 350, "priority": 2, "reason": "夜间气温接近零度" }
    ],
    "packingTips": [
      "最重的装备贴近背部中上部，行走重心最稳",
      "睡袋、备用衣物压包底，到营才取",
      "急救包和头灯放顶袋，伸手可及"
    ]
  }
}
```

- `items[].priority`：1 必带 / 2 建议 / 3 可选；`category` 沿用装备库 1~11 枚举，LLM 输出非法值时归入 11 其他
- `weightGrams` 可空（LLM 估不准就不编）；`items` 上限 20 条
- 错误返回：参数校验 400；LLM 与规则都失败 → 500（极小概率）；未登录 401

### 装包位置口径（前端展示用，后端不存）

品类 → 背包装载位置固定映射（借鉴主流重装徒步的装包原则）：

| 位置 | 品类 | 原则 |
|---|---|---|
| 包底 | 2 帐篷天幕、3 睡袋防潮垫、6 炊具饮水 | 大而轻/到营才取 |
| 背部中段 | 1 背包自身、10 电子设备 | 最重贴背，重心稳 |
| 包内上部 | 5 服装、11 其他 | 路上偶取 |
| 顶袋 | 7 照明、9 安全急救 | 随手可取，夜幕/受伤即刻能用 |
| 随身 | 4 鞋袜、8 导航 | 不进包 |

### 常用装备目录（v1.2 增补）——「别让用户填表」的底座

**动机**：让用户手动录入每件装备的名称/品类/克重是不想用的第一大原因。平台内置一份**常用徒步装备目录**（通用名 + 真实克重 + 品类），所有输入名称的地方（装备库录入、清单临时条目）都做**即时联想**：输入「冲锋」→ 匹配「冲锋衣 450g · 服装」等，点选即自动补齐品类与克重。

- 表 `gear_catalog`：**平台级共享数据，无 user_id**（区别于用户装备库）；迁移预置 130+ 条——通用装备 + **主力品牌型号**（Gregory/Osprey/凯乐石/三峰/MSR/黑冰/Salomon/始祖鸟/Petzl/Garmin 等，含 brand 列与真实克重），输入「格里高利」按品牌列命中，「gregory」大小写不敏感
- `GET /gear/items/catalog?keyword=&limit=10`：按 name 前缀/包含排序的联想查询（内存过滤，平台量级恒定小）；keyword 可空（返回前 N 条供浏览）
- 只读：无写接口；目录数据随迁移演进（品牌型号已预置，可继续扩子类）
- 前端联想规范：输入 ≥1 字符、250ms 防抖、下拉展示「名称 + 品牌 + 克重」（品类以 GearIcon 图标呈现），点选回填；选不到就按原逻辑自由填写

### 可视化装包（v1.2 增补）——拼图式交互

清单详情弹窗新增**视图切换（列表 / 装包拼图）**。装包视图：

- **背包轮廓 SVG**（侧面剖面：主仓 + 顶袋盖 + 底仓）作画布，五个「仓位」绝对定位在轮廓对应区域（随身 / 顶袋 / 背部中段 / 包内上部 / 包底），与装包位置口径一致
- 每件装备渲染为**品类图标芯片**（GearIcon：1 背包 / 2 帐篷 / 3 睡袋 / 4 鞋 / 5 服装 / 6 炊具 / 7 灯 / 8 指南针 / 9 急救 / 10 电池 / 11 盒子）
- **拖拽即打包**：把左侧「待装」条目拖进背包 = `checked=1`（打包装入），从背包拖回待装 = `checked=0`——拼图式可视化的语义是真实的打包状态，不是摆设
- 拖拽悬停时，被拖条目**按品类推荐的仓位**高亮提示（该放哪由装备性质决定，落点只决定打没打包，显示严格按品类归位）
- 装包进度：背包轮廓旁显示 已装 X 件 · 总重 Y kg，装满的仓位芯片自动流式排列

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：装备库 4 接口 + 打包清单 8 接口；条目快照式弱引用装备库；清单复制；级联删除 |
| 2026-10-12 | v1.1 增补：POST /gear/suggest（OpenAI 兼容 LLM + 规则降级）；装包位置口径 |
| 2026-10-13 | v1.2 增补：GET /gear/catalog 常用装备目录（~60 条预置）；可视化装包（背包轮廓 + 拖拽即打包）；GearIcon 品类图标体系 |
| 2026-10-25 | v1.3 勘误：目录接口实际路径为 `GET /gear/items/catalog`（v1.2 行与提交信息均为 `/gear/catalog` 简写笔误，线上从未存在该路径）；品牌型号预置后下拉展示含品牌；与代码批量核对确认 15 个接口一致 |
