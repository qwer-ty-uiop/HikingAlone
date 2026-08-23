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