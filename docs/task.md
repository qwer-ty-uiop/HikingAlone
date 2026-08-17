# 一人徒（HikingAlone）任务进度

> 更新：2026-08-17

## 项目结构

- 后端 `HikingAlone/`：Spring Boot 3.5.16（8080）+ MyBatis-Plus 3.5.17 + springdoc 2.9.0，DDD 4 层（domain/application/infrastructure/controller），按模块分包（index、training），Result/Exception 在根包
- 前端 `HikingAlone-frontend/`：React 18 / Vite 5 / Tailwind 3.3.5，开发端口 5174，vite 代理 `/home`、`/train` → :8080
- 接口文档：`docs/interfaces/`（docs-first：先写文档再编码）
- 全局约束：`@RequiredArgsConstructor` + final 字段（禁用 @Autowired 字段注入）；不用魔数（用 `common/enums` 枚举）；`@Transactional` 双写；前端只用已实现的后端接口；图标用 @phosphor-icons/react
- **地图已下线**（2026-08-17）：因未购买高德地图套餐，前端地图组件整体移除（`src/map/`、`public/env.js`、`public/poi-categories.json`、`src/types/amap.d.ts`、index.html 高德 loader、index.css 覆盖物样式、App 的 /map 路由与首页入口）。后续如需恢复：重新申请 Web端(JS API) Key，恢复 env.js 与 loader 加载即可

## 已完成

### 后端（/train 训练模块）
- 9 个接口全部实现并编译通过（`./mvnw -o clean compile`）：计划 CRUD/状态流转/打卡提交/详情/热力图/最近记录等
- 周期功能：`training_plan` 新增 `cycle_type tinyint DEFAULT 0`、`cycle_anchor smallint DEFAULT NULL`；周期可选 每天/每周/每周x/每月x/每年x（anchor 语义：weekday 1-7 / day 1-31 / month*100+day）
- 周期语义：周期计划永续运行，不因时间自动流转状态；结束仅通过放弃（软删除）/物理删除；历史"已完成/已过期"周期计划读取时自动恢复为进行中
- 惰性周期重置：无定时任务，进度/达标/剩余仅统计当前周期内 `training_record_daily`（`periodStart/periodEnd` 由自然日历对齐 anchor 计算）
- 双写模型：`training_record`（事件表）+ `training_record_daily`（日汇总表）同事务
- 状态机重构：`refreshStatus` 拆为 `refreshCycleStatus` / `refreshOneTimeStatus`，圈复杂度达标
- `TrainingPlanVO` 含 `periodStart/periodEnd`；`PlanCreateDTO/PlanUpdateDTO` 支持 cycleType/cycleAnchor

### 前端（/train 页面）
- design-taste 重构：状态列 auto-fit 网格 `grid-cols-[repeat(auto-fit,minmax(260px,1fr))]`；计划按状态分列，4 状态全部可见
- 计划搜索：按标题/描述过滤（搜索时计数）
- 自定义下拉（portal 面板替代原生 picker/select）：ItemSelect（训练项）、DateField（日期，button trigger + 自绘日历）
- 周期计划表单：PlanFormModal 周期类型 segmented + anchor 输入（cycleType ≥2 显示），校验 + buildCycleAnchor
- 周期记录视图（PlanDetailModal）：`periodOf` 与后端 currentPeriod 语义一致；本期卡片高亮（进度条 + 最近 N 天展开）+ 历史周期折叠组（3 个可见 + 显示更早）；非周期按天折叠（默认最近 3 天）
- 最近提交可内联编辑；热力图浅色卡片全宽 + hover tooltip；打卡输入/拖拽双模式；CountUp 用 useMotionValueEvent 订阅
- 全局 `user-select: none`（input/textarea/select/contenteditable 恢复），杜绝点击出现输入光标
- 滚动条迭代（最终态）：删除自定义 `.no-scrollbar`/`.inset-scrollbar`，弹窗内容区用原生滚动条；打卡卡片底部边框距离 `pb-10` 移到**弹窗外壳**上（滚动容器自身不加 padding），`overflow-hidden` 裁剪圆角，滚动条上下箭头完整、不超出边框

### 踩坑记录
- `package.json` 为 `"type": "module"`，Node 脚本须用 `.cjs` 扩展名
- input 内日期文本无法用 CSS user-select 阻止选中 → 触发元素改 button
- 白屏根因多为 stale HMR（旧模块缓存），硬刷新 + 重启 dev server
- 滚动条"内边距不足/溢出圆角/缺下箭头"根因：滚动条轨道贯穿滚动容器自身 padding → 边框距离必须放在滚动容器外（外壳 padding），且外壳需 overflow-hidden 裁剪

## 待办 / 后续工作

1. **后端数据库变更（用户重启后端前执行，如尚未执行）**
   ```sql
   ALTER TABLE training_plan ADD COLUMN cycle_type tinyint DEFAULT 0;
   ALTER TABLE training_plan ADD COLUMN cycle_anchor smallint DEFAULT NULL;
   ```
2. **后端只能由用户启动/重启**（约定）；需重启验证周期接口
3. 前端滚动条修复已编译通过（tsc + build），待用户在浏览器刷新确认视觉效果（上下箭头完整、圆角裁剪正确、底部 40px 留白）
4. 未复现/未验证项：headless 自动化复现计划卡点击问题（曾因渲染上下文不对中断，非必须）
5. 收尾：诊断残留已清理（ov-repro.cjs、截图、puppeteer-core 已卸载）；node_modules 中无临时依赖
