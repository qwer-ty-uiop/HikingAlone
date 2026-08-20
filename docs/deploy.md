# 一人徒 云服务器部署手册（裸机 + systemd + Nginx）

> 2026-08-17 与 GitHub Actions CI/CD 配套。CI 推送 jar / dist 到服务器，服务器只需一次性初始化。

## 架构

```
浏览器 → Nginx(:80) ─┬─ /home、/train/**（API）→ 后端(:8080)；/train 页面刷新回 index.html
                     ├─ / 及静态资源 → 前端静态文件(/var/www/hikingalone)
                     └─ 其余未知路径 → 真实 404
```

- 后端：Spring Boot 3.5.16 jar，systemd 管理，profile=prod
- 前端：Vite 构建产物 dist，Nginx 静态托管
- 数据库：MySQL 8，与后端同机 `127.0.0.1:3306/hiking_alone`（`application-prod.yml` 默认配置）

## 一、服务器一次性初始化

### 1. 安装 JDK 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
java -version   # 确认 21
```

### 2. 安装 MySQL 8 并初始化

```bash
sudo apt install -y mysql-server
sudo systemctl enable --now mysql
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root'; FLUSH PRIVILEGES;"
sudo mysql -e "CREATE DATABASE IF NOT EXISTS hiking_alone DEFAULT CHARACTER SET utf8mb4;"
```

> 用户名/密码与 `application-prod.yml`（root/root）保持一致；生产环境建议改强密码并同步更新 yml。

建表 + 种子数据（与 CI 里同一套脚本，注意顺序）：

```bash
cd /tmp && git clone <后端仓库URL> hikingalone-src && cd hikingalone-src
mysql -uroot -proot hiking_alone < sql/ddl.sql
mysql -uroot -proot hiking_alone < sql/data.sql
rm -rf /tmp/hikingalone-src
```

### 3. 安装 Nginx

```bash
sudo apt install -y nginx
```

创建站点配置 `/etc/nginx/conf.d/hikingalone.conf`：

```nginx
server {
    listen 80;
    server_name _;

    root /var/www/hikingalone;
    index index.html;

    # 1. 静态资源：存在则长缓存；缺失直接 404（避免资源缺失被 HTML 兜底掩盖，难排查）
    location ~* \.(?:js|css|png|jpe?g|gif|svg|ico|woff2?|ttf|otf|eot|webp|avif|map)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
        try_files $uri =404;
    }

    # 2. 后端 API + 前端 /train 路由分流。
    #    /home、/train、/user 及其子路径不带尾斜杠也在此正则内（GET /home、GET /train 都是无斜杠请求）。
    #    浏览器刷新 /train 页面（Accept 含 text/html）时回前端路由 index.html；
    #    其余请求（fetch，Accept: */* 等）反代到后端 —— 与前端 vite proxy 的 bypass 逻辑一致。
    location ~ ^/(home|train|user)(/.*)?$ {
        if ($http_accept ~* text/html) {
            rewrite ^ /index.html last;
        }
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 3. 首页：直接返回 index.html；其余未知路径返回真实 404（前端只有 / 和 /train 两个路由）
    location / {
        try_files $uri $uri/ =404;
    }

    # 4. index.html 不缓存（内容随构建变化，引用带 hash 的 assets）
    location = /index.html {
        add_header Cache-Control "no-cache";
    }
}
```

> 分流说明：前端只有 `/` 和 `/train` 两个路由（自绘路由，精确匹配），只有这两个路径需要回 index.html，未知路径（如 `/foo`）返回真实 404。`/home`、`/train`、`/user` 的 API 请求都不带尾斜杠（`fetch('/home')`、`GET /train`、`POST /user/login`），必须用正则 `^/(home|train|user)(/.*)?$` 匹配，`location /home/` 这类带斜杠前缀匹配不到。`/home`、`/user` 只有 API 且不是前端路由，无需 Accept 分流；`/train` 既是前端路由又是 API 前缀，必须分流。

```bash
sudo mkdir -p /var/www/hikingalone
sudo nginx -t && sudo systemctl reload nginx
```

### 4. 后端 systemd 服务

创建 `/etc/systemd/system/hikingalone.service`：

```ini
[Unit]
Description=HikingAlone backend
After=network.target mysql.service

[Service]
User=app
Group=app
WorkingDirectory=/opt/hikingalone
ExecStart=/usr/bin/java -jar /opt/hikingalone/app.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
TimeoutStopSec=60

[Install]
WantedBy=multi-user.target
```

```bash
sudo mkdir -p /opt/hikingalone
sudo systemctl daemon-reload
sudo systemctl enable hikingalone
sudo systemctl start hikingalone
journalctl -u hikingalone -f   # 看启动日志
```

### 5. 目录权限（供 CI 的 scp 写入）

CI 用 scp 把 jar 传到 `/tmp/hikingalone/`、dist 传到 `/tmp/hikingalone-frontend/`，再通过 sudo 移动到目标目录。所以 `/tmp` 下无需额外权限，部署用户在服务器上需有 sudo 权限（`sudo mv` / `systemctl restart` 用到）。

## 二、GitHub Secrets 配置

两个仓库各自 Settings → Secrets and variables → Actions → New repository secret：

| Secret | 值 |
| --- | --- |
| `DEPLOY_HOST` | 云服务器公网 IP |
| `DEPLOY_USER` | SSH 登录用户名（需有 sudo 权限） |
| `DEPLOY_SSH_KEY` | 该用户可用的私钥（服务器上 `~/.ssh/authorized_keys` 要配好对应公钥） |

> 不配 secrets 时 CI 正常跑，CD 的 deploy job 会失败——属预期，配好后再推送 main 即自动部署。

## 三、CI/CD 流程说明

- **CI**（push / PR 到 main 都触发）：后端在 MySQL 容器（自动执行 ddl + data）上跑 `mvnw verify`（含 SpringBootTest）；前端 `npm ci` + `tsc && vite build`。产物分别上传 artifact。
- **CD**（仅 push 到 main 触发）：后端 jar → `/opt/hikingalone/app.jar` 并 `systemctl restart`；前端 dist → `/var/www/hikingalone/`（先清空再移动）。
