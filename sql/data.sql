-- ============================================
-- 首页测试数据
-- 库：hiking_alone
-- ============================================

-- 导航菜单（含一个父菜单 + 两个子菜单）
INSERT INTO nav_menu (id, name, link_url, parent_id, sort) VALUES
(1, '首页',        '/',          0, 1),
(2, '路线',        '/routes',    0, 2),
(3, '活动',        '/activities',0, 3),
(4, '我的',        '/profile',   0, 4),
(5, '热门路线',    '/routes/hot', 2, 1),
(6, '近期活动',    '/activities/recent', 3, 1);

-- 首页横幅（3 条启用，1 条禁用用于测试过滤）
INSERT INTO home_banner (id, title, image_url, link_url, sort, status, create_time, update_time) VALUES
(1, '秋季山野徒步季',     'https://picsum.photos/seed/banner1/1600/600',  '/routes',        1, 1, NOW(), NOW()),
(2, '新路线上线：黄山云海', 'https://picsum.photos/seed/banner2/1600/600',  '/routes/huangshan', 2, 1, NOW(), NOW()),
(3, '周末活动招募中',     'https://picsum.photos/seed/banner3/1600/600',  '/activities',     3, 1, NOW(), NOW()),
(4, '已下架的横幅',       'https://picsum.photos/seed/banner4/1600/600',  '/disabled',       4, 0, NOW(), NOW());

-- ============================================
-- 训练模块测试数据（固定 user_id=1）
-- ============================================

-- 训练计划
INSERT INTO training_plan (id, user_id, title, description, start_date, end_date, status, create_time, update_time) VALUES
(1, 1, '居家健身挑战', '8月居家健身，冲刺百公里徒步前的体能储备', '2026-08-10', '2026-08-30', 1, NOW(), NOW()),
(2, 1, '周末拉练计划', '每周两次山野拉练', '2026-08-01', '2026-08-31', 1, NOW(), NOW()),
(3, 1, '上月已过期计划', '测试过期状态', '2026-07-01', '2026-07-31', 3, NOW(), NOW());

-- 训练项（计划1含两个训练项：times + sets 各一个）
INSERT INTO training_plan_item (id, plan_id, name, mode, total_times, total_sets, unit, sort) VALUES
(1, 1, '俯卧撑', 'times', 100, NULL, '个', 1),
(2, 1, '深蹲',   'sets',  30, 3, '个', 2),
(3, 2, '山野徒步', 'times', 60, NULL, '公里', 1),
(4, 3, '晨跑',   'times', 50, NULL, '公里', 1);

-- 训练记录（append 事件流：每次提交一条；update_time 未编辑为 NULL）
INSERT INTO training_record (id, plan_id, item_id, user_id, record_date, completed_sets, completed_times, create_time, update_time) VALUES
(1, 1, 1, 1, '2026-08-12', 0, 30, NOW(), NULL),
(2, 1, 1, 1, '2026-08-13', 0, 25, NOW(), NULL),
(3, 1, 2, 1, '2026-08-14', 1, 30, NOW(), NULL),
(4, 2, 3, 1, '2026-08-09', 0, 25, NOW(), NULL),
(5, 2, 3, 1, '2026-08-15', 0, 35, NOW(), NULL);

-- 每日汇总（与上述事件记录双写对应：每训练项每天一行）
INSERT INTO training_record_daily (id, plan_id, item_id, user_id, record_date, total_times, total_sets, commit_count, create_time, update_time) VALUES
(1, 1, 1, 1, '2026-08-12', 30, 0, 1, NOW(), NOW()),
(2, 1, 1, 1, '2026-08-13', 25, 0, 1, NOW(), NOW()),
(3, 1, 2, 1, '2026-08-14', 30, 1, 1, NOW(), NOW()),
(4, 2, 3, 1, '2026-08-09', 25, 0, 1, NOW(), NOW()),
(5, 2, 3, 1, '2026-08-15', 35, 0, 1, NOW(), NOW());
