-- hiking_alone.nav_menu 定义

create table if not exists`nav_menu` (
    `id`        bigint not null AUTO_INCREMENT,
    `name`      varchar(100) default null COMMENT '导航名称',
    `link_url`  varchar(500) default null COMMENT '跳转地址',
    `parent_id` bigint       default '0' COMMENT '父菜单id，做下拉子菜单',
    `sort`      int          default '0' COMMENT '排序等级',
    primary key (`id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.home_banner 定义

create table if not exists `home_banner` (
    `id`          bigint not null AUTO_INCREMENT,
    `title`       varchar(200) default null COMMENT '标题',
    `image_url`   varchar(500) default null COMMENT '图片地址',
    `link_url`    varchar(500) default null COMMENT '跳转链接',
    `sort`        int          default '0' COMMENT '排序号',
    `status`      tinyint      default '1' COMMENT '0禁用 1启用',
    `create_time` datetime     default null,
    `update_time` datetime     default null,
    primary key (`id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.training_plan 定义

create table if not exists `training_plan` (
    `id`          bigint not null AUTO_INCREMENT,
    `user_id`     bigint       default null COMMENT '所属用户',
    `title`       varchar(100) default null COMMENT '计划标题，如：居家健身挑战',
    `description` varchar(500) default null COMMENT '计划描述',
    `start_date`  date         default null COMMENT '周期开始日期',
    `end_date`    date         default null COMMENT '周期结束日期',
    `cycle_type`  tinyint      default '0' COMMENT '周期类型：0不重复 1每天 2每周 3每月 4每年',
    `cycle_anchor` smallint    default null COMMENT '周期锚点：每周=星期(1周一~7周日)；每月=日(1~31)；每年=月*100+日(如815=8月15日)；null=默认锚点',
    `status`      tinyint      default '1' COMMENT '0已放弃 1进行中 2已完成 3已过期',
    `create_time` datetime     default null,
    `update_time` datetime     default null,
    primary key (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.training_plan_item 定义

create table if not exists `training_plan_item` (
    `id`          bigint not null AUTO_INCREMENT,
    `plan_id`     bigint       default null COMMENT '所属计划',
    `name`        varchar(100) default null COMMENT '训练项名称，如：俯卧撑',
    `mode`        varchar(10)  default null COMMENT '制定模式：times=按次数, sets=按次数+组数',
    `total_times` int          default null COMMENT '目标次数（times模式=总次数；sets模式=每组次数）',
    `total_sets`  int          default null COMMENT '目标组数（仅sets模式）',
    `unit`        varchar(20)  default null COMMENT '单位，如：个/组/公里',
    `sort`        int          default '0' COMMENT '排序',
    primary key (`id`),
    KEY           `idx_plan_id` (`plan_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.training_record 定义（append 事件流：每次提交追加一条，同日可多条，不可变日志）

create table if not exists `training_record` (
    `id`              bigint not null AUTO_INCREMENT,
    `plan_id`         bigint   default null COMMENT '所属计划',
    `item_id`         bigint   default null COMMENT '所属训练项',
    `user_id`         bigint   default null COMMENT '所属用户',
    `record_date`     date     default null COMMENT '记录日期',
    `completed_sets`  int      default '0' COMMENT '当天完成组数（sets模式；times模式为0）',
    `completed_times` int      default '0' COMMENT '当天完成次数（times模式=当天总次数；sets模式=每组次数，默认取计划值）',
    `create_time`     datetime default null COMMENT '本次提交时间',
    `update_time`     datetime default null COMMENT '最近编辑时间（未编辑为 null）',
    primary key (`id`),
    KEY               `idx_plan_date` (`plan_id`, `record_date`, `create_time`),
    KEY               `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.training_record_daily 定义（每日汇总表：每训练项每天一行，plan+item+date 唯一，随提交双写维护）

create table if not exists `training_record_daily` (
    `id`           bigint not null AUTO_INCREMENT,
    `plan_id`      bigint   default null COMMENT '所属计划',
    `item_id`      bigint   default null COMMENT '所属训练项',
    `user_id`      bigint   default null COMMENT '所属用户',
    `record_date`  date     default null COMMENT '记录日期',
    `total_times`  int      default '0' COMMENT '当日累计次数（times模式=Σcompleted_times；sets模式=Σ每组次数）',
    `total_sets`   int      default '0' COMMENT '当日累计组数（sets模式=Σcompleted_sets；times模式=0）',
    `commit_count` int      default '0' COMMENT '当日提交次数（热力图 count 数据源）',
    `create_time`  datetime default null,
    `update_time`  datetime default null,
    primary key (`id`),
    unique key `uk_plan_item_date` (`plan_id`, `item_id`, `record_date`),
    KEY            `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;
-- hiking_alone.route_track 定义（徒步路线轨迹：上传轨迹点，里程/时长/爬升由领域层计算，轨迹点 JSON 同表存储）

create table if not exists `route_track` (
    `id`             bigint       not null AUTO_INCREMENT,
    `user_id`        bigint       default null COMMENT '所属用户',
    `name`           varchar(100) default null COMMENT '路线名称',
    `description`    varchar(500) default null COMMENT '路线描述',
    `distance`       decimal(10,2) default null COMMENT '总里程（公里，按轨迹点 Haversine 计算）',
    `duration_min`   int          default null COMMENT '总时长（分钟，轨迹点时间戳首尾差）',
    `elevation_gain` decimal(10,1) default null COMMENT '累计爬升（米，相邻点高程正向差累加）',
    `points_json`    mediumtext   default null COMMENT '轨迹点 JSON：[{"lng":..,"lat":..,"ele":..,"time":".."}]（MEDIUMTEXT 16MB，避免长轨迹超出 TEXT 65KB 上限）',
    `waypoints_json` mediumtext   default null COMMENT '途径点/命名标注点 JSON：[{"lng":..,"lat":..,"ele":..,"name":".."}]，来自 GPX wpt / KML Point，可为空',
    `create_time`    datetime     default null,
    `update_time`    datetime     default null,
    primary key (`id`),
    KEY              `idx_user_id` (`user_id`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.user 定义（用户账号：登录凭证；`user` 为 MySQL 保留字，必须加反引号）

create table if not exists `user` (
    `id`          bigint not null AUTO_INCREMENT,
    `username`    varchar(100)  default null COMMENT '用户名（唯一）',
    `password`    varchar(255)  default null COMMENT '密码（当前明文，生产前改 BCrypt）',
    `email`       varchar(255)  default null COMMENT '邮箱（登录账号，唯一）',
    `create_time` datetime      default null,
    `update_time` datetime      default null,
    primary key (`id`),
    unique key `uk_username` (`username`),
    unique key `uk_email` (`email`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.body_metric 定义（身体数据模块，见 docs/interfaces/body.md）

create table if not exists `body_metric` (
    `id`                 bigint       not null AUTO_INCREMENT,
    `user_id`            bigint       not null COMMENT '所属用户',
    `record_date`        date         not null COMMENT '记录日期（每用户每天一条，同日重复提交覆盖更新）',
    `weight_kg`          decimal(5,1) not null COMMENT '体重 kg（25.0~250.0）',
    `resting_heart_rate` int          default null COMMENT '晨起静息心率 次/分（30~220，可空）',
    `note`               varchar(200) default null COMMENT '备注（睡眠/疲劳/状态等，≤200字）',
    `create_time`        datetime     default null COMMENT '首次记录时间',
    `update_time`        datetime     default null COMMENT '最近修改时间',
    primary key (`id`),
    unique key `uk_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB default CHARSET=utf8mb4 collate=utf8mb4_0900_ai_ci;

-- hiking_alone.hike_journal 定义（徒步日记模块，见 docs/interfaces/journal.md）

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

-- hiking_alone.daily_checkin 定义（每日打卡模块：饮水/步数/睡眠，见 docs/interfaces/daily.md）

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

-- hiking_alone.gear_item 定义（装备模块：装备台账，见 docs/interfaces/gear.md）

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

-- hiking_alone.gear_checklist 定义（装备模块：打包清单头，见 docs/interfaces/gear.md）

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

-- hiking_alone.gear_checklist_item 定义（装备模块：清单条目，随清单级联删除；对装备库为快照式弱引用）

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

-- hiking_alone.hike_wish 定义（徒步心愿单模块，见 docs/interfaces/wish.md）

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

-- hiking_alone.hike_goal 定义（目标模块，见 docs/interfaces/goal.md；month=0 为年度哨兵值）

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

-- hiking_alone.knowledge_note 定义（徒步知识库模块，见 docs/interfaces/knowledge.md）

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
