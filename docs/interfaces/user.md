# 用户模块接口设计

> 文档版本：v1 · 2026-08-20 · 状态：已实现（后端已按此设计编码）

## 概述

用户模块提供注册、登录、修改密码；登录成功后建立 **服务端 `HttpSession` 会话**，`/train` 等需要登录的接口从会话取当前用户 id，未登录返回 401。

约束：

- 密码**明文存储**（学习项目，未做哈希）；生产前需改为 BCrypt
- 登录成功后在 `HttpSession` 写入 `loginUserId`，后续请求携带会话 Cookie（`JSESSIONID`）即视为已登录
- 所有响应统一 `Result<T>` 格式；错误经 `GlobalExceptionHandler` 统一返回中文 message
- 错误响应 HTTP 状态码与 body `code` 一致：400 参数/校验错误、401 未登录、404 接口不存在、405 方法不支持、500 未预期异常（前端按 body code 处理，HTTP 码供监控/排障）

## 数据模型

| 表 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户账号（登录凭证） | username, password, email, create_time, update_time |

唯一约束：`username`、`email` 唯一（业务层已校验，建表加唯一索引兜底）。

## 会话机制

- 登录接口 `POST /user/login` 成功后：`request.getSession().setAttribute("loginUserId", account.id)`，并下发 `JSESSIONID` Cookie。
- 需要登录的接口（全部业务模块 `/train/**`、`/routes/**`、`/body/**`、`/journal/**`、`/daily/**`、`/gear/**`、`/wish/**`、`/goal/**`、`/achievement/**`、`/stats/**`、`/knowledge/**`、`/profile/**` 及 `/user/me`、`/user/logout`；放行 `/user/register|login|forget` 与公开的 `/home`、`/email`）由 `LoginUserInterceptor` 拦截：会话无 `loginUserId` 时返回 HTTP 401 + `Result.error(401, "未授权，请先登录")`。
- 登出 `POST /user/logout` 使会话失效（`session.invalidate()`）。
- 前端 `fetch` 需携带 Cookie（`credentials: 'include'` 或同源默认携带）。

## 接口清单

### 1. POST /user/register —— 注册

请求体（`UserCreateDTO`）：

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| username | string | 是 | 非空 |
| password | string | 是 | 非空 |
| email | string | 是 | 邮箱格式 |
| code | string | 是 | 4 位验证码（先 POST /email/code 获取） |

业务规则：先校验邮箱验证码（错误/过期 → 400「验证码错误或已过期」，验证码一次性消费）→ 用户名已存在 → 400「用户名已存在」；邮箱已被注册 → 400「邮箱已被注册」。

> 验证码流程见 [email.md](./email.md)。

响应：`data` 为 `{ username, email }`。

```json
{ "code": 200, "message": "操作成功", "data": { "username": "hiker", "email": "hiker@example.com" }, "timestamp": 1755331200000 }
```

### 2. POST /user/login —— 登录

请求体（`UserLoginDTO`）：

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| email | string | 是 | 邮箱格式 |
| password | string | 是 | 非空 |

业务规则：按邮箱取账号，校验密码；失败统一返回 400「邮箱或密码错误」（不泄露账号是否存在）。

响应：`data` 为 `{ email }`；成功后建立会话（见上）。

```json
{ "code": 200, "message": "操作成功", "data": { "email": "hiker@example.com" }, "timestamp": 1755331200000 }
```

### 3. POST /user/forget —— 修改密码

请求体（`UserChangePasswordDTO`）：

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| email | string | 是 | 邮箱格式 |
| code | string | 是 | 4 位验证码（先 POST /email/code 获取） |
| oldPassword | string | 是 | 非空 |
| newPassword | string | 是 | 非空 |

业务规则：先校验邮箱验证码（错误/过期 → 400「验证码错误或已过期」，一次性消费）→ 账号不存在 → 400「账号不存在」；旧密码错误 → 400「旧密码错误」；新密码非空。

> 验证码流程见 [email.md](./email.md)。

响应：`data` 为 `null`。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

### 4. POST /user/logout —— 登出（需登录）

无请求体。使当前会话失效。

响应：`data` 为 `null`。

```json
{ "code": 200, "message": "操作成功", "data": null, "timestamp": 1755331200000 }
```

### 5. GET /user/me —— 当前登录用户（需登录，2026-10-25 补记）

无请求参数。**会话校验接口**：前端页面加载时调用，校准本地登录态与服务端会话是否一致，杜绝会话失效后仍显示已登录。

- 由 `LoginUserInterceptor` 拦截：未登录返回 HTTP 401（前端据此跳登录页）
- 已登录：从会话上下文取 userId 查账号，仅返回邮箱（不返回密码等敏感字段）

响应 `data`：

| 字段 | 类型 | 说明 |
|---|---|---|
| email | string | 登录账号邮箱 |

```json
{ "code": 200, "message": "操作成功", "data": { "email": "hiker@example.com" }, "timestamp": 1755331200000 }
```

## 待办

- 密码明文存储：生产前改 BCrypt 哈希
- 会话超时/并发控制、remember-me 等暂未实现
