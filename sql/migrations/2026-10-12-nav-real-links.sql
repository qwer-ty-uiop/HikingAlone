-- =====================================================================
-- 2026-10-12 导航与横幅死链清理：/activities、/profile、/routes/hot 等
-- 首页可点击的死链改为真实模块入口（/routes/hot 同日实现，见 docs/interfaces/route.md）
-- nav_menu 为管理配置表，直接重建；home_banner 仅修正死链
-- =====================================================================

-- 1. 重建导航：顶级（首页/路线/训练/日记/我的）+ 二级（热门路线、复盘三件套、我的下的打卡族）
DELETE FROM `nav_menu`;
INSERT INTO `nav_menu` (`id`, `name`, `link_url`, `parent_id`, `sort`) VALUES
(1, '首页',     '/',          0, 1),
(2, '路线',     '/routes',    0, 2),
(3, '训练打卡', '/train',     0, 3),
(4, '徒步日记', '/journal',   0, 4),
(5, '我的',     '/profile',   0, 5),
(6, '热门路线', '/routes/hot', 2, 1),
(7, '心愿单',   '/wish',       4, 1),
(8, '成就徽章', '/achievement',4, 2),
(9, '年度报告', '/stats',      4, 3),
(10, '每日打卡', '/daily',      5, 1),
(11, '身体数据', '/body',       5, 2),
(12, '目标',     '/goal',       5, 3),
(13, '装备清单', '/gear',       5, 4),
(14, '知识库',   '/knowledge',  5, 5);

-- 2. 横幅死链修正（标题顺带对齐真实功能）
UPDATE `home_banner` SET `link_url` = '/routes' WHERE `id` = 1;
UPDATE `home_banner` SET `link_url` = '/routes/hot' WHERE `id` = 2;
UPDATE `home_banner` SET `title` = '定个小目标，月月有山爬', `link_url` = '/goal' WHERE `id` = 3;
UPDATE `home_banner` SET `link_url` = '/profile' WHERE `id` = 4;
