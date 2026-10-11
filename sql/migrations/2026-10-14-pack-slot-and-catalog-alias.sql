-- =====================================================================
-- 2026-10-14 装包体验修复：
-- 1) gear_checklist_item 增加 slot（装载仓位 0随身 1顶袋 2背部中段 3上部 4包底，
--    null=按品类推荐）——装包拼图支持自由拖拽换位并持久化，而不是被品类绑死
-- 2) gear_catalog 增加 search_terms（中文品牌别名/拼音），修复「格里高利B65」
--    这类中文输入匹配不到拉丁品牌名的问题；并补录格里高利 B65 等型号
-- =====================================================================

-- 1. 清单条目装载仓位（幂等守卫）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gear_checklist_item' AND COLUMN_NAME = 'slot');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `gear_checklist_item` ADD COLUMN `slot` tinyint default null COMMENT ''装载仓位 0随身 1顶袋 2背部中段 3上部 4包底（null=按品类推荐）'' AFTER `sort`',
    'SELECT ''slot column already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 目录别名列（幂等守卫）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'gear_catalog' AND COLUMN_NAME = 'search_terms');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `gear_catalog` ADD COLUMN `search_terms` varchar(200) default null COMMENT ''联想别名（中文品牌名/拼音/型号缩写，空格分隔）'' AFTER `brand`',
    'SELECT ''search_terms column already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 全量重建目录种子：138 条 + 中文别名 + 补录常见型号（含格里高利 B65）
DELETE FROM `gear_catalog`;
INSERT INTO `gear_catalog` (`name`, `category`, `brand`, `search_terms`, `weight_grams`, `sort`) VALUES
-- ===== 通用（别名空） =====
('徒步背包 20L（一日轻装）', 1, NULL, NULL, 600, 1),
('徒步背包 30L（一日重装）', 1, NULL, NULL, 900, 2),
('徒步背包 40L（两日轻装）', 1, NULL, NULL, 1300, 3),
('徒步背包 55L（多日重装）', 1, NULL, NULL, 1700, 4),
('背包防雨罩', 1, NULL, NULL, 120, 5),
('水袋 2L', 1, NULL, NULL, 180, 6),
('腰包', 1, NULL, NULL, 100, 7),
('单人帐篷', 2, NULL, NULL, 1500, 1),
('双人帐篷', 2, NULL, NULL, 2100, 2),
('天幕', 2, NULL, NULL, 800, 3),
('地布', 2, NULL, NULL, 300, 4),
('帐篷地钉套组', 2, NULL, NULL, 150, 5),
('睡袋（舒适 0℃）', 3, NULL, NULL, 1100, 1),
('睡袋（舒适 -10℃）', 3, NULL, NULL, 1500, 2),
('睡袋内胆', 3, NULL, NULL, 250, 3),
('蛋槽防潮垫', 3, NULL, NULL, 400, 4),
('充气防潮垫', 3, NULL, NULL, 550, 5),
('充气枕', 3, NULL, NULL, 90, 6),
('中帮徒步鞋', 4, NULL, NULL, 1200, 1),
('越野跑鞋', 4, NULL, NULL, 750, 2),
('速干徒步袜', 4, NULL, NULL, 50, 3),
('防水袜', 4, NULL, NULL, 60, 4),
('雪套', 4, NULL, NULL, 250, 5),
('简易冰爪（四齿）', 4, NULL, NULL, 400, 6),
('冲锋衣', 5, NULL, NULL, 450, 1),
('冲锋裤', 5, NULL, NULL, 380, 2),
('抓绒中间层', 5, NULL, NULL, 350, 3),
('羽绒服（轻量）', 5, NULL, NULL, 500, 4),
('速干 T 恤', 5, NULL, NULL, 150, 5),
('速干长裤', 5, NULL, NULL, 220, 6),
('防晒帽', 5, NULL, NULL, 80, 7),
('保暖帽', 5, NULL, NULL, 90, 8),
('保暖手套', 5, NULL, NULL, 80, 9),
('轻量雨衣', 5, NULL, NULL, 250, 10),
('备用袜子', 5, NULL, NULL, 50, 11),
('防晒冰袖', 5, NULL, NULL, 40, 12),
('气炉头', 6, NULL, NULL, 90, 1),
('气罐 230g', 6, NULL, NULL, 350, 2),
('套锅套装', 6, NULL, NULL, 300, 3),
('运动水壶 1L', 6, NULL, NULL, 120, 4),
('保温杯 500ml', 6, NULL, NULL, 300, 5),
('餐具套装', 6, NULL, NULL, 120, 6),
('打火机/防风火柴', 6, NULL, NULL, 20, 7),
('能量补给（坚果/能量胶）', 6, NULL, NULL, 150, 8),
('头灯', 7, NULL, NULL, 90, 1),
('备用电池', 7, NULL, NULL, 40, 2),
('营地灯', 7, NULL, NULL, 150, 3),
('手机（离线地图）', 8, NULL, NULL, 200, 1),
('指南针', 8, NULL, NULL, 30, 2),
('纸质地图', 8, NULL, NULL, 50, 3),
('手持 GPS', 8, NULL, NULL, 150, 4),
('急救包（基础）', 9, NULL, NULL, 200, 1),
('救生毯', 9, NULL, NULL, 60, 2),
('防晒霜', 9, NULL, NULL, 80, 3),
('驱蚊液', 9, NULL, NULL, 100, 4),
('求生哨', 9, NULL, NULL, 20, 5),
('个人药品包', 9, NULL, NULL, 150, 6),
('充电宝 10000mAh', 10, NULL, NULL, 200, 1),
('相机（微单）', 10, NULL, NULL, 500, 2),
('无人机', 10, NULL, NULL, 900, 3),
('对讲机（单只）', 10, NULL, NULL, 250, 4),
('登山杖（单根）', 11, NULL, NULL, 240, 1),
('垃圾袋', 11, NULL, NULL, 20, 2),
('纸巾', 11, NULL, NULL, 30, 3),
('墨镜', 11, NULL, NULL, 30, 4),
('结绳 5m', 11, NULL, NULL, 120, 5),
-- ===== 品牌型号（背包） =====
('Gregory Paragon 48L', 1, 'Gregory', '格里高利 格里高利paragon', 1490, 101),
('Gregory Baltoro 65L', 1, 'Gregory', '格里高利', 2190, 102),
('格里高利 B65', 1, 'Gregory', '格里高利b65 gregory b65 格里高利', 1700, 103),
('Osprey Atmos AG 65L', 1, 'Osprey', '小鹰 osprey atmos', 1930, 104),
('Osprey Kestrel 68L', 1, 'Osprey', '小鹰', 1900, 105),
('Osprey Talon 44L', 1, 'Osprey', '小鹰', 1300, 106),
('Osprey Stratos 36L', 1, 'Osprey', '小鹰', 1520, 107),
('凯乐石 曙光 40L', 1, '凯乐石 KAILAS', 'kailas', 1250, 108),
('凯乐石 磐石 55L', 1, '凯乐石 KAILAS', 'kailas', 1550, 109),
('三峰出云 40L', 1, '三峰 3F UL', '3f 三峰出云', 1480, 110),
('三峰晴空 20L', 1, '三峰 3F UL', '3f', 620, 111),
('Deuter Futura 32L', 1, 'Deuter', '多特', 1450, 112),
('Deuter Aircontact 65+10L', 1, 'Deuter', '多特', 2300, 113),
('Granite Gear Crown 60L', 1, 'Granite Gear', '花岗岩', 1390, 114),
('ULA Circuit 60L', 1, 'ULA', 'ula circuit', 1590, 115),
('强氧 漩涡 55L', 1, '强氧', NULL, 1580, 116),
('HMG DCF 轻量背包 50L', 1, 'Hyperlite', '大力马 hmg', 900, 117),
-- ===== 品牌型号（帐篷天幕） =====
('三峰 苍穹 2人', 2, '三峰 3F UL', '3f', 2600, 201),
('三峰 静星 15D 单人', 2, '三峰 3F UL', '3f', 1300, 202),
('牧高笛 冷山 2人', 2, '牧高笛', 'mobi garden', 2300, 203),
('自由之魂 云雀 2人', 2, '自由之魂', 'tfs', 1900, 204),
('MSR Hubba Hubba 2人', 2, 'MSR', 'msr hubba', 1740, 205),
('Nemo Dagger OSMO 2人', 2, 'Nemo', 'nemo dagger', 1900, 206),
('Big Agnes Copper Spur HV UL2', 2, 'Big Agnes', 'ba 比格尼斯', 1420, 207),
('黑鹿 素原 2人', 2, '黑鹿', 'blackdeer', 2400, 208),
('Hilleberg Nallo 2', 2, 'Hilleberg', '希勒伯格 黑帐篷', 2100, 209),
-- ===== 品牌型号（睡袋防潮垫） =====
('黑冰 Z700 羽绒睡袋', 3, '黑冰', 'blackice z700', 1080, 301),
('天石 G700 羽绒睡袋', 3, '天石', 'highrock', 980, 302),
('Nemo Disco 15℉ 羽绒睡袋', 3, 'Nemo', 'nemo disco', 1250, 303),
('RAB Neutrino 400 羽绒睡袋', 3, 'RAB', 'rab neutrino', 800, 304),
('Sea to Summit Spark SpI', 3, 'Sea to Summit', 'sts', 540, 305),
('Therm-a-Rest Hyperion 20℉', 3, 'Therm-a-Rest', 'thermarest', 680, 306),
('凯乐石 G10 700蓬 睡袋', 3, '凯乐石 KAILAS', 'kailas', 780, 307),
('冰岩 T10 羽绒睡袋', 3, '冰岩', NULL, 850, 308),
-- ===== 品牌型号（鞋袜） =====
('Salomon X Ultra 4 GTX（单只）', 4, 'Salomon', '萨洛蒙 salomon xultra', 370, 401),
('La Sportiva TX4（单只）', 4, 'La Sportiva', '拉思珀蒂瓦 lasportiva', 380, 402),
('Scarpa Kailash Trek（对）', 4, 'Scarpa', '斯卡帕', 1400, 403),
('Merrell Moab 3（对）', 4, 'Merrell', '迈乐', 1000, 404),
('Lowa Renegade GTX Mid（对）', 4, 'Lowa', '洛瓦', 1500, 405),
('凯乐石 DG 2.0（对）', 4, '凯乐石 KAILAS', 'kailas dg', 1300, 406),
('Altra Lone Peak 8（单只）', 4, 'Altra', NULL, 315, 407),
-- ===== 品牌型号（服装） =====
('始祖鸟 Beta LT 冲锋衣', 5, 'Arc''teryx', '始祖鸟鸟 arcteryx beta lt', 485, 501),
('Patagonia Torrentshell 3L 冲锋衣', 5, 'Patagonia', '巴塔哥尼亚 巴塔 torrentshell', 416, 502),
('RAB Kinetic 2.0 冲锋衣', 5, 'RAB', 'rab kinetic', 430, 503),
('凯乐石 Mont X 冲锋衣', 5, '凯乐石 KAILAS', 'kailas montx', 380, 504),
('mont-bell Storm Cruiser 冲锋衣', 5, 'mont-bell', '蒙贝欧 montbell', 330, 505),
('始祖鸟 Atom 轻量棉服', 5, 'Arc''teryx', '始祖鸟鸟 atom', 385, 506),
('Rab Microlight 羽绒服', 5, 'RAB', 'rab', 490, 507),
('凯乐石 9A 硬壳裤', 5, '凯乐石 KAILAS', 'kailas 9a', 320, 508),
-- ===== 品牌型号（炊具饮水） =====
('SOTO Amicus 炉头', 6, 'SOTO', 'soto amicus', 100, 601),
('火枫 FMS-116T 炉头', 6, '火枫 Fire-Maple', 'firemaple fms', 108, 602),
('BRS-3000T 钛合金炉头', 6, 'BRS 兄弟', 'brs', 25, 603),
('Jetboil MiniMo 一体炉', 6, 'Jetboil', '捷保 jetboil', 400, 604),
('MSR PocketRocket 2 炉头', 6, 'MSR', 'msr pocketrocket', 73, 605),
('Snow Peak SOD-320 炉头', 6, 'Snow Peak', '雪峰 snowpeak', 145, 606),
('GSI Halulite 套锅', 6, 'GSI', 'gsi', 280, 607),
-- ===== 品牌型号（照明） =====
('Petzl Actik Core 头灯', 7, 'Petzl', '攀索 petzl actik', 88, 701),
('奈特科尔 NU25 UL 头灯', 7, 'Nitecore', 'nitecore nu25', 43, 702),
('Black Diamond Spot 400 头灯', 7, 'Black Diamond', '黑钻 bd spot', 102, 703),
('山力士 曙光 3 头灯', 7, '山力士', NULL, 65, 704),
-- ===== 品牌型号（导航） =====
('Garmin inReach Mini 2 卫星通讯', 8, 'Garmin', '佳明 garmin inreach', 100, 801),
('Garmin eTrex 22x 手持 GPS', 8, 'Garmin', '佳明 garmin etrex', 205, 802),
('Garmin fenix 7 户外手表', 8, 'Garmin', '佳明 garmin fenix 飞耐时', 73, 803),
('高驰 APEX 2 Pro 户外手表', 8, 'COROS', 'coros apex', 60, 804),
-- ===== 品牌型号（安全急救） =====
('Adventure Medical Kit .5 急救包', 9, 'AMK', 'amk', 120, 901),
('小站 徒步急救包', 9, '小站', NULL, 150, 902),
-- ===== 品牌型号（电子设备） =====
('Anker 充电宝 10000mAh', 10, 'Anker', '安克 anker', 212, 1001),
('GoPro HERO12 运动相机', 10, 'GoPro', 'gopro', 154, 1002),
('大疆 Mini 4 Pro 无人机', 10, 'DJI', 'dji 大疆mini', 249, 1003),
-- ===== 品牌型号（其他） =====
('Black Diamond Trail 登山杖（对）', 11, 'Black Diamond', '黑钻 bd', 540, 1101),
('LEKI Makalu 登山杖（对）', 11, 'LEKI', '雷基 leki', 560, 1102),
('Fizan Compact 超轻登山杖（单根）', 11, 'Fizan', 'fizan', 85, 1103),
('三峰 钛合金登山杖（单根）', 11, '三峰 3F UL', '3f', 160, 1104);
