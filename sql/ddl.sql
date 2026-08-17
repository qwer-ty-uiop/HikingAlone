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