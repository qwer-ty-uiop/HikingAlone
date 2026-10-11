# 徒步知识库模块接口设计

> 文档版本：v1 · 2026-10-11 · 状态：按此设计编码

## 概述

知识库是徒步用户的**个人笔记**：安全守则、急救步骤、结绳方法、LNT 无痕山林原则、某条线路的情报……散在聊天记录和收藏夹里的经验，沉淀成可检索的自己的手册。与日记的区别：日记记「某一天的事」，知识笔记存「长期有效的方法」。

约束：需登录、只用 `GET`/`POST`、统一 `Result<T>`、前端只调已实现接口。

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `knowledge_note` | 知识笔记 | user_id, title, category, content, pinned |

取值范围（领域实体不变量）：

| 字段 | 范围 | 说明 |
|---|---|---|
| `title` | [1, 100] 字符 | 如「失温的三个阶段与处置」 |
| `category` | [1, 7] | 1 安全守则 / 2 急救 / 3 装备技巧 / 4 线路情报 / 5 环保准则 / 6 营养补给 / 7 其他 |
| `content` | [1, 10000] 字符 | 纯文本（前端按换行分段渲染），trim 后落库 |
| `pinned` | 0 / 1 | 置顶，1 在前 |

索引：`idx_user_id (user_id)`。

## 领域结构（DDD 四层）

```
domain/knowledge
  └── entity/KnowledgeNote.java            # 聚合根：工厂 create() / update()
  └── repository/KnowledgeNoteRepository.java # save / findById / listByUser / delete

application/knowledge
  └── KnowledgeService.java                # 编排 + 关键词过滤（内存过滤，个人量级无需全文索引）

infrastructure/knowledge
  ├── mapper/KnowledgeNoteMapper.java
  ├── po/KnowledgeNotePO.java
  └── repository/impl/KnowledgeNoteRepositoryImpl.java

interfaces/knowledge
  ├── KnowledgeController.java
  ├── converter/KnowledgeConverter.java
  ├── dto/command/KnowledgeSaveDTO.java / KnowledgeIdDTO.java
  └── vo/query/KnowledgeNoteVO.java
```

## 接口清单

### 1. POST /knowledge —— 新建笔记

```json
{ "title": "失温的三个阶段与处置", "category": 2, "content": "轻度：寒战、语无伦次……\n中度：寒战停止是危险信号……" }
```

新建 pinned=0。响应：落库后的 KnowledgeNoteVO。

### 2. POST /knowledge/update —— 编辑笔记

请求体 `{ id, title?, category?, content?, pinned? }`（非 null 覆盖）。

### 3. POST /knowledge/delete —— 删除笔记

请求体 `{ id }`。物理删除。

### 4. GET /knowledge/list?keyword=&category= —— 笔记列表

- `keyword` 可空，非空时对 title + content 做 `contains` 内存过滤（个人量级 ≤ 数千条，可接受）
- `category` 可空，非空时只返回该分类
- 排序：pinned 降序 → update_time 倒序
- 响应 `data` 为 KnowledgeNoteVO 数组（含全量 content，前端列表直读、详情免请求）

```json
{
  "code": 200, "message": "操作成功", "timestamp": 1760000000000,
  "data": [{
    "id": 3, "title": "失温的三个阶段与处置", "category": 2,
    "categoryLabel": "急救", "content": "轻度：寒战……", "pinned": 1,
    "createTime": "2026-10-11T09:00:00", "updateTime": null
  }]
}
```

## 业务规则

- 归属过滤同其他模块
- 关键词过滤在应用服务内存完成；数据量增长到需要全文索引时再演进（预留）
- 错误返回：参数校验 400；未登录 401；未预期 500

## 变更记录

| 日期 | 说明 |
|---|---|
| 2026-10-11 | v1 定稿：POST /knowledge、/knowledge/update、/knowledge/delete + GET /knowledge/list（关键词 + 分类过滤，置顶优先） |
