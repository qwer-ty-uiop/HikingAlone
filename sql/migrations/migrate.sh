#!/usr/bin/env bash
# =====================================================================
# SQL 迁移执行器：按文件名顺序执行 pending 迁移，执行结果记录在
# _migration_history 表中，重复调用只跑新文件 —— 供 CI/CD 与人工使用。
#
# 用法: migrate.sh <db_user> <db_password> <db_name> <migrations_dir> [db_host]
# 约定:
#   - 迁移文件命名 YYYY-MM-DD-<slug>.sql，字典序 = 执行顺序
#   - 已应用的文件绝不修改，修 bug 请新增迁移文件
#   - MySQL DDL 不支持事务：单个文件失败即中止（CD 变红），
#     文件内语句尽量写成可重入（IF [NOT] EXISTS / INSERT IGNORE）
# =====================================================================
set -euo pipefail

DB_USER="${1:?usage: migrate.sh <user> <password> <database> <migrations_dir> [host]}"
DB_PASSWORD="${2:?missing db password}"
DB_NAME="${3:?missing db name}"
MIGRATIONS_DIR="${4:?missing migrations dir}"
# 显式走 TCP：CI 的 mysql 是容器服务、无本地 socket；服务器本机 socket 也在 127.0.0.1 监听
DB_HOST="${5:-127.0.0.1}"

# 密码走环境变量，不出现在进程列表
export MYSQL_PWD="$DB_PASSWORD"

run_sql() { mysql --default-character-set=utf8mb4 --host="$DB_HOST" -u"$DB_USER" "$DB_NAME" "$@"; }

# 1. 追踪表（独立于业务表，名字固定前缀下划线，ddl.sql 不建它）
run_sql <<'SQL'
CREATE TABLE IF NOT EXISTS `_migration_history` (
    `filename`   varchar(255) NOT NULL COMMENT '迁移文件名（唯一）',
    `applied_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '应用时间',
    PRIMARY KEY (`filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;
SQL

# 2. 收集待执行文件（字典序；文件名白名单防注入与误执行）
mkdir -p "$MIGRATIONS_DIR"
cd "$MIGRATIONS_DIR"
shopt -s nullglob
files=(*.sql)
if [ ${#files[@]} -eq 0 ]; then
  echo "no migration files in $MIGRATIONS_DIR"
  exit 0
fi

applied="$(run_sql -N -B -e 'SELECT filename FROM _migration_history')"

applied_count=0
skipped_count=0
for f in "${files[@]}"; do
  if ! echo "$f" | grep -Eq '^[A-Za-z0-9][A-Za-z0-9._-]*$'; then
    echo "FATAL: illegal migration filename '$f' (use YYYY-MM-DD-slug.sql)"; exit 1
  fi
  if echo "$applied" | grep -Fxq "$f"; then
    skipped_count=$((skipped_count + 1))
    echo "skip   $f (already applied)"
    continue
  fi
  echo "apply  $f"
  run_sql < "$f"
  run_sql -e "INSERT INTO _migration_history (filename) VALUES ('$f')"
  applied_count=$((applied_count + 1))
done

echo "migrations done: $applied_count applied, $skipped_count skipped"
