-- =====================================================================
-- 一人徒 · 2026-10-11 功能批次（8 个新模块，10 项新功能）
-- 生产变更脚本：可重复执行（create table if not exists）
-- 适用库：hiking_alone（MySQL 8.0）
-- 与 sql/ddl.sql 保持同构；CI/CD 部署时也会自动执行 ddl.sql，本脚本供手工变更/留档
--
-- 模块清单：
--   1. hike_journal        徒步日记（docs/interfaces/journal.md）
--   2. daily_checkin       每日打卡：饮水/步数/睡眠（docs/interfaces/daily.md）
--   3. gear_item           装备库（docs/interfaces/gear.md）
--   4. gear_checklist      打包清单（docs/interfaces/gear.md）
--   5. gear_checklist_item 打包清单条目（docs/interfaces/gear.md）
--   6. hike_wish           徒步心愿单（docs/interfaces/wish.md）
--   7. hike_goal           目标追踪（docs/interfaces/goal.md）
--   8. knowledge_note      徒步知识库（docs/interfaces/knowledge.md）
--   （成就徽章 / 年度统计为零表模块，无需建表）
-- =====================================================================

-- 1. 徒步日记：每次徒步一条，可编辑；关联轨迹可自动带出里程/时长/爬升

create table if not exists `hike_journal` (
    `id`             bigint        not null AUTO_INCREMENT,
    `user_id`        bigint        not null COMMENT '所属用户',
    `title`          varchar(100)  not null COMMENT '日记标题（1~100字）',
    `hike_date`      date          not null COMMENT '徒步日期（≤今天）',
    `route_id`       bigint        default null COMMENT '关联轨迹 id（可空；须属于当前用户）',
    `distance_km`    decimal(6,2)  default null COMMENT '里程 km（0,500]，关联轨迹且未填时自动带出',
    `duration_min`   int           default null COMMENT '时长 分钟 [1,4320]',
    `elevation_gain` decimal(7,1)  default null COMMENT '累计爬升 m [0,10000]',
    `difficulty`     tinyint       not null COMMENT '难度 1休闲 2轻松 3进阶 4挑战 5地狱',
    `mood`           tinyint       not null COMMENT '心情 1疲惫 2平静 3愉悦 4兴奋 5震撼',
    `weather`        varchar(50)   default null COMMENT '天气描述（≤50字）',
    `companions`     varchar(100)  default null COMMENT '同伴（独行/同伴名，≤100字）',
    `cost`           decimal(10,2) default null COMMENT '本次总花费 [0,999999.99]',
    `content`        text          default null COMMENT '正文（≤8000字）',
    `create_time`    datetime      default null,
    `update_time`    datetime      default null,
    primary key (`id`),
    KEY              `idx_user_date` (`user_id`, `hike_date`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 2. 每日打卡：每用户每天一行，POST 只覆盖非 null 字段（饮水/步数/睡眠互不干扰）

create table if not exists `daily_checkin` (
    `id`            bigint        not null AUTO_INCREMENT,
    `user_id`       bigint        not null COMMENT '所属用户',
    `record_date`   date          not null COMMENT '记录日期（每用户每天一条，部分字段独立覆盖）',
    `water_cups`    int           default null COMMENT '饮水 杯（0~30，250ml/杯）',
    `steps`         int           default null COMMENT '步数（0~200000）',
    `sleep_hours`   decimal(3,1)  default null COMMENT '睡眠时长 小时 (0,24]',
    `sleep_quality` tinyint       default null COMMENT '睡眠质量 1很差~5很好',
    `create_time`   datetime      default null,
    `update_time`   datetime      default null,
    primary key (`id`),
    unique key `uk_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 3. 装备库：装备台账；退役（status=0）不删，保留台账与历史清单对应关系

create table if not exists `gear_item` (
    `id`           bigint       not null AUTO_INCREMENT,
    `user_id`      bigint       not null COMMENT '所属用户',
    `name`         varchar(100) not null COMMENT '装备名（1~100字）',
    `category`     tinyint      not null COMMENT '品类 1背包 2帐篷天幕 3睡袋防潮垫 4鞋袜 5服装 6炊具饮水 7照明 8导航 9安全急救 10电子设备 11其他',
    `weight_grams` int          default null COMMENT '自重 克 [0,30000]',
    `brand`        varchar(50)  default null COMMENT '品牌（≤50字）',
    `note`         varchar(200) default null COMMENT '备注（≤200字）',
    `status`       tinyint      default '1' COMMENT '0已退役 1在役',
    `create_time`  datetime     default null,
    `update_time`  datetime     default null,
    primary key (`id`),
    KEY            `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 4. 打包清单头

create table if not exists `gear_checklist` (
    `id`          bigint       not null AUTO_INCREMENT,
    `user_id`     bigint       not null COMMENT '所属用户',
    `title`       varchar(100) not null COMMENT '清单标题（1~100字）',
    `hike_date`   date         default null COMMENT '对应出行日期',
    `note`        varchar(200) default null COMMENT '备注（≤200字）',
    `status`      tinyint      default '0' COMMENT '0准备中 1已打包 2已结束',
    `create_time` datetime     default null,
    `update_time` datetime     default null,
    primary key (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 5. 打包清单条目：删清单级联删条目；条目为装备库快照（name/weight_grams 落在条目上）

create table if not exists `gear_checklist_item` (
    `id`           bigint       not null AUTO_INCREMENT,
    `checklist_id` bigint       not null COMMENT '所属清单',
    `gear_item_id` bigint       default null COMMENT '来源装备 id（弱引用，仅标记来源）',
    `name`         varchar(100) not null COMMENT '条目名（添加时快照）',
    `weight_grams` int          default null COMMENT '克重快照 [0,30000]',
    `checked`      tinyint      default '0' COMMENT '0未打包 1已打包',
    `sort`         int          default '0' COMMENT '排序 [0,9999]',
    `create_time`  datetime     default null,
    primary key (`id`),
    KEY            `idx_checklist_id` (`checklist_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 6. 徒步心愿单：种草 → 计划 → 完成（可挂日记）/ 放弃，终态不可回退

create table if not exists `hike_wish` (
    `id`            bigint        not null AUTO_INCREMENT,
    `user_id`       bigint        not null COMMENT '所属用户',
    `title`         varchar(100)  not null COMMENT '心愿标题（1~100字）',
    `location`      varchar(200)  default null COMMENT '地点描述（≤200字）',
    `expected_date` date          default null COMMENT '期望出发日期',
    `priority`      tinyint       default '2' COMMENT '优先级 1随缘 2想去 3一定要去',
    `note`          varchar(500)  default null COMMENT '攻略/季节备忘（≤500字）',
    `status`        tinyint       default '0' COMMENT '0想去 1已计划 2已完成 3已放弃',
    `done_date`     date          default null COMMENT '完成日期',
    `journal_id`    bigint        default null COMMENT '完成后关联的日记 id（须属于当前用户）',
    `create_time`   datetime      default null,
    `update_time`   datetime      default null,
    primary key (`id`),
    KEY             `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 7. 目标追踪：进度读时从 hike_journal 聚合；month=0 为年度哨兵值

create table if not exists `hike_goal` (
    `id`           bigint         not null AUTO_INCREMENT,
    `user_id`      bigint         not null COMMENT '所属用户',
    `metric`       tinyint        not null COMMENT '指标 1月里程km 2月次数 3月爬升m 4年里程km 5年次数 6年爬升m',
    `target_value` decimal(10,1)  not null COMMENT '目标值（按 metric 钳制）',
    `period_type`  tinyint        not null COMMENT '周期 1月度 2年度',
    `year`         int            not null COMMENT '年份 [2020,2100]',
    `month`        int            not null default '0' COMMENT '月 [1,12]；年度固定 0（哨兵值，保证唯一索引）',
    `status`       tinyint        default '1' COMMENT '0已放弃 1进行中（达成与否读时计算）',
    `create_time`  datetime       default null,
    `update_time`  datetime       default null,
    primary key (`id`),
    unique key `uk_user_period_metric` (`user_id`, `year`, `month`, `metric`),
    KEY        `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- 8. 徒步知识库：个人长期有效的方法论笔记

create table if not exists `knowledge_note` (
    `id`          bigint        not null AUTO_INCREMENT,
    `user_id`     bigint        not null COMMENT '所属用户',
    `title`       varchar(100)  not null COMMENT '笔记标题（1~100字）',
    `category`    tinyint       not null COMMENT '分类 1安全守则 2急救 3装备技巧 4线路情报 5环保准则 6营养补给 7其他',
    `content`     text          not null COMMENT '正文（1~10000字）',
    `pinned`      tinyint       default '0' COMMENT '0普通 1置顶',
    `create_time` datetime      default null,
    `update_time` datetime      default null,
    primary key (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;
