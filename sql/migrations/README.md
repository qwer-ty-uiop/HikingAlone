# sql/migrations —— 增量数据库迁移

`sql/ddl.sql` 是**全量建表**（`create table if not exists`，只管新表）；对已有表的结构变更（`ALTER TABLE`）与数据订正**不能**靠它，必须走本目录的**增量迁移**，由 CI/CD 自动执行，无需手动上服务器。

## 机制

- CI（build job）会在全新 MySQL 上先执行 `ddl.sql` + `data.sql`，再跑一遍 `migrate.sh` —— 迁移脚本有问题直接挡在构建阶段
- CD（deploy job）把 `sql/migrations/` 上传到服务器 `/opt/hikingalone/migrations/`，然后执行 `migrate.sh`：
  - 服务端用 `_migration_history` 表记录已应用文件
  - 按文件名字典序执行 pending 迁移，每个文件只执行一次
  - 失败即中止（部署变红），该文件不记录，下次部署自动重试

## 约定（必须遵守）

1. **命名**：`YYYY-MM-DD-<slug>.sql`（如 `2026-10-15-add-journal-rating.sql`），字典序即执行顺序
2. **已应用的文件绝不修改** —— 修问题请新增迁移文件（历史表按文件名判重）
3. **可重入**：MySQL DDL 无事务，单个文件失败可能留下半成品。语句尽量写成可重入：
   - `CREATE TABLE IF NOT EXISTS` / `DROP TABLE IF EXISTS`
   - MySQL 8.0 无 `ADD COLUMN IF NOT EXISTS`：新增列前可用存储过程判断，或接受「失败后人工确认再重跑」
   - 数据订正用 `INSERT IGNORE` / 带 `WHERE NOT EXISTS`
4. **一个文件做一件事**，保持小而清晰

## 手动执行（应急）

```bash
bash /opt/hikingalone/migrations/migrate.sh root <password> hiking_alone /opt/hikingalone/migrations
```
