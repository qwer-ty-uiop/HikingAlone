# 邮箱验证码模块接口设计

> 文档版本：v2 · 2026-08-23 · 状态：已实现（方案 B：验证码存储走领域仓库接口）

## 概述

邮箱验证码用于**证明邮箱归属**，注册（`POST /user/register`）与修改密码（`POST /user/forget`）都必须先通过验证码校验。

设计要点：

- 验证码**不返回给前端**，只通过邮件送达；接口只返回 `{ sent }` 表示是否已发送
- 验证码**一次性消费**：校验通过后立即从存储删除，防止重放
- **冷却机制**：同一邮箱 60s 内不能重复发送（`sent=false`），防刷
- **有效期**：5 分钟（300s），过期自动失效
- **发件人必须与 SMTP 认证用户一致**：`SimpleMailMessage` 必须显式 `setFrom(spring.mail.username)`，否则 QQ 服务器返回 `501 Mail from address must be same as authorization user`（JavaMailSender 不会自动拿 username 当发件人）
- 存储维度是 **email**（不是 code）——校验时只需定位「该邮箱存的那条验证码」

## 领域结构（方案 B：存储抽象进基础设施层）

验证码的存取属于「持久化」职责，按 DDD 应落在基础设施层；但 Redis 的 key/value 组装细节
又不需要像关系型 Mapper 那样复杂。权衡结果——**领域层定义语义接口，Redis 实现收敛在基础设施层**：

```
domain/email
  ├── entity/VerificationCode.java          # 纯领域对象：生成 4 位随机码 + 计算两个存储 key（email 维度）
  └── repository/VerificationCodeStore.java # 领域仓库接口：isCooling / markCooling / saveCode / getCode / removeCode

infrastructure/email/repository/impl
  └── RedisVerificationCodeStore.java       # Redis 实现：RedisTemplate 存取，key 带 email:code: / email:cool: 前缀

application/email
  ├── EmailService.java                     # 应用服务：只依赖 VerificationCodeStore 接口，不感知 Redis
  ├── cmd/GetCodeCmd.java                   # { email }
  └── cmd/VerifyCodeCmd.java                # { email, code }
```

为什么这样权衡：

- **应用层不再出现 `RedisTemplate`**——如果以后把验证码缓存换成 Caffeine/本地内存，
  只需新增一个实现类，`EmailService` 与领域层零改动（依赖倒置）
- **key 拼接留在领域实体**：`VerificationCode` 持有 `codeKey`/`coolKey`，
  存储 key 的生成是业务语义（邮箱维度），不是存储技术细节
- **接口方法即业务动词**：`isCooling`、`markCooling`、`saveCode`、`getCode`、`removeCode`，
  与 Redis 的 `hasKey`/`set`/`get`/`delete` 一一对应但语义化

### VerificationCode 实体

| 成员 | 说明 |
|---|---|
| `getCode(email)` | 工厂：生成 4 位随机码（`%04d`），并预计算两个 key —— **发送场景用** |
| `ofEmail(email)` | 工厂：只按邮箱算 key，不生成随机码 —— **校验场景用**（校验不需要新码，避免白生成一个再丢弃） |
| `REDIS_PREFIX_CODE` | `email:code:`，验证码本体 key 前缀 |
| `REDIS_PREFIX_COOL` | `email:cool:`，冷却标记 key 前缀（value 仅占位） |

### VerificationCodeStore 接口（领域层）

| 方法 | 语义 |
|---|---|
| `isCooling(code)` | 冷却标记存在即冷却中，返回 true 时不应再发送 |
| `markCooling(code, seconds)` | 写冷却标记，seconds 秒自动过期 |
| `saveCode(code, seconds)` | 存验证码本体，seconds 秒自动过期 |
| `getCode(code)` | 取验证码本体；未发送/已过期返回 null |
| `removeCode(code)` | 删除验证码（校验通过后一次性消费） |

### RedisVerificationCodeStore（基础设施层）

`RedisTemplate<String, String>` 存取；key 来自领域实体，value 与过期时间如上表。所有 Redis
细节（key 前缀、TTL、占位 value）都收敛在本类。

## 接口清单

### 1. POST /email/code —— 发送验证码

请求体（`EmailDTO`）：

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| email | string | 是 | 邮箱格式 |

业务规则：冷却期内（`email:cool:` 存在）直接返回 `{ sent: false }`，不发送邮件、不刷新冷却；否则写冷却标记（60s）→ 存验证码（300s）→ 发邮件，返回 `{ sent: true }`。

响应：`data` 为 `{ sent }`（**不含验证码本身**）。

```json
{ "code": 200, "message": "操作成功", "data": { "sent": true }, "timestamp": 1755331200000 }
```

### 2. POST /email/verify —— 校验验证码（供其他服务调用，暂未暴露独立业务接口）

请求体（`EmailVerifyDTO`）：

| 字段 | 类型 | 必填 | 校验 |
|---|---|---|---|
| email | string | 是 | 邮箱格式 |
| code | string | 是 | 4 位数字 |

业务规则：取出该邮箱存储的验证码比对；一致 → 删除（一次性消费）→ 返回 true；不一致/已过期 → 返回 false。返回值为 `Boolean`，不抛异常。

```json
{ "code": 200, "message": "操作成功", "data": true, "timestamp": 1755331200000 }
```

> 实际业务入口在注册/改密：`UserService` 在业务校验前调用 `emailService.verify(...)`，返回 false 时抛 400「验证码错误或已过期」。

## 配置项（application.yml）

| 配置 | 默认值 | 说明 |
|---|---|---|
| `spring.mail.host/port/username/password` | — | SMTP 服务（发送方身份） |
| `register.code.expire` | 300 | 验证码有效期（秒） |
| `register.code.cool` | 60 | 同邮箱发送冷却（秒） |

## 时序图

```
前端                    EmailController          EmailService             VerificationCodeStore     SMTP
 │  POST /email/code         │                       │                          │                   │
 │──────────────────────────▶│                       │                          │                   │
 │                           │  toSendEmailCmd       │                          │                   │
 │                           │──────────────────────▶│  isCooling               │                   │
 │                           │                       │─────────────────────────▶│                   │
 │                           │                       │◀─────────────────────────│ (false=可发送)    │
 │                           │                       │  markCooling(60s)        │                   │
 │                           │                       │─────────────────────────▶│                   │
 │                           │                       │  saveCode(码,300s)       │                   │
 │                           │                       │─────────────────────────▶│                   │
 │                           │                       │  SimpleMailMessage       │                   │
 │                           │                       │─────────────────────────────────────────────▶│
 │◀─ { sent: true } ─────────│◀──────────────────────│                          │                   │
```

校验时（注册/改密内部）：`verify` → `getCode` → 比对 → `removeCode`（一次性）。
