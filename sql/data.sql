-- ============================================
-- 首页测试数据
-- 库：hiking_alone
-- ============================================

-- 导航菜单：顶级（首页/路线/训练/日记/我的）+ 二级（全部真实模块入口）
INSERT INTO nav_menu (id, name, link_url, parent_id, sort) VALUES
(1, '首页',     '/',           0, 1),
(2, '路线',     '/routes',     0, 2),
(3, '训练打卡', '/train',      0, 3),
(4, '徒步日记', '/journal',    0, 4),
(5, '我的',     '/profile',    0, 5),
(6, '热门路线', '/routes/hot', 2, 1),
(7, '心愿单',   '/wish',       4, 1),
(8, '成就徽章', '/achievement',4, 2),
(9, '年度报告', '/stats',      4, 3),
(10, '每日打卡', '/daily',      5, 1),
(11, '身体数据', '/body',       5, 2),
(12, '目标',     '/goal',       5, 3),
(13, '装备清单', '/gear',       5, 4),
(14, '知识库',   '/knowledge',  5, 5);

-- 首页横幅（3 条启用，1 条禁用用于测试过滤；链接均为真实路由）
INSERT INTO home_banner (id, title, image_url, link_url, sort, status, create_time, update_time) VALUES
(1, '秋季山野徒步季',       'https://picsum.photos/seed/banner1/1600/600',  '/routes',     1, 1, NOW(), NOW()),
(2, '新路线上线：黄山云海', 'https://picsum.photos/seed/banner2/1600/600',  '/routes/hot', 2, 1, NOW(), NOW()),
(3, '定个小目标，月月有山爬', 'https://picsum.photos/seed/banner3/1600/600', '/goal',       3, 1, NOW(), NOW()),
(4, '已下架的横幅',         'https://picsum.photos/seed/banner4/1600/600',  '/profile',    4, 0, NOW(), NOW());
