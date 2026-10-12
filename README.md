# 个人博客

一个基于 **Vue 3 + Vite + Spring Boot 3** 的前后端分离个人博客。提供文章、归档、爱好专栏、留言板、音乐播放器和管理后台，支持配置首页背景、作者介绍、站点图标及关于页面。

仓库地址：<https://github.com/sigle1/blog>

> 本仓库不提供数据库密码、JWT 密钥、API Key 或默认管理员账号。克隆后需要自行初始化数据库并配置环境变量。当前代码仍有生产部署限制，见文末说明。

## 目录

- [功能与页面](#功能与页面)
- [技术栈与目录结构](#技术栈与目录结构)
- [环境准备](#环境准备)
- [数据库初始化](#数据库初始化)
- [配置与启动后端](#配置与启动后端)
- [创建管理员](#创建管理员)
- [配置与启动前端](#配置与启动前端)
- [QQ 登录](#qq-登录)
- [自定义内容与素材](#自定义内容与素材)
- [构建与部署](#构建与部署)
- [常见问题](#常见问题)
- [提交文件与当前限制](#提交文件与当前限制)

## 功能与页面

| 功能 | 内容 |
|---|---|
| 首页 | 背景轮播、文章列表、作者介绍、公告与时钟 |
| 文章与归档 | 文章详情、分类标签、评论、按年份归档 |
| 爱好专栏 | 爱好列表、专栏内容及文章 |
| 留言板 | 留言详情、评论及互动 |
| 音乐 | 播放器、播放列表与歌词相关组件 |
| 关于 | 作者介绍和关于页面配置 |
| 管理后台 | 文章、笔记、爱好、评论、留言、音乐及站点配置 |
| 登录 | 用户名密码登录、可选 QQ 授权登录 |
| AI 后端 | 兼容 OpenAI 协议的聊天服务；前端旧 `/ai` 路由当前跳转首页 |

常用页面：`/`、`/articles`、`/archive`、`/hobbies`、`/messages`、`/music`、`/about`。管理登录页是 `/admin/login`，后台入口是 `/admin`。

## 技术栈与目录结构

| 部分 | 技术 |
|---|---|
| 前端 | Vue 3、Vite 5、Vue Router、Pinia、Axios、Element Plus、Ant Design Vue |
| 后端 | Java 17、Spring Boot 3.4.10、MyBatis、JWT、Spring AI |
| 存储 | MySQL；项目声明了 Redis 支持 |
| 测试 | Spring Boot Test、H2 测试数据库 |

```text
blog/
├── README.md
├── .gitignore
├── backend/
│   ├── pom.xml
│   ├── CONFIGURATION.md
│   └── src/
│       ├── main/java/                  # 控制器、服务、模型、认证
│       ├── main/resources/
│       │   ├── application.yml         # 环境变量引用与公共配置
│       │   ├── mapper/                 # MyBatis SQL 映射
│       │   ├── sql/                    # 基础表和迁移脚本
│       │   └── db/                     # 其他数据库迁移
│       └── test/                       # 测试及测试配置
└── frontend/
    ├── package.json
    ├── package-lock.json
    ├── .env.example
    ├── vite.config.js
    ├── public/                         # 静态素材
    └── src/
        ├── api/                        # API 请求封装
        ├── assets/                     # 头像和背景等素材
        ├── components/                 # 公共组件与播放器
        ├── config/                     # 音乐、备案等配置
        ├── router/                     # 页面路由
        ├── store/、stores/             # 状态管理
        ├── utils/                      # Markdown、图片、歌词等工具
        └── views/                      # 用户页面及 admin 后台
```

## 环境准备

需要 Java 17、Maven 3.9、Node.js 18+、npm 和 MySQL 8。Redis 的地址及密码按实际使用情况配置。后端当前会创建 AI 组件，即使不使用 AI 页面，也需要提供 AI 配置。

```powershell
git clone https://github.com/sigle1/blog.git
cd blog
java -version
mvn -version
node -v
npm -v
```

以下本地启动示例使用 Windows PowerShell。终端里的 `$env:` 变量只对该终端及其子进程有效，后端命令需在设置变量的同一窗口执行。

## 数据库初始化

1. 创建自己的数据库，例如：

```sql
CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 使用 MySQL 客户端、Workbench 或其他数据库工具选择这个数据库，执行 `backend/src/main/resources/sql/create_tables.sql`。该脚本创建用户、文章、评论、爱好、笔记、留言和音乐等基础表，**不能在已有表的数据库中盲目重复运行**。

3. 按需要核对并执行迁移脚本：

| 文件 | 用途 |
|---|---|
| `migration_home_hero_settings.sql` | 首页设置表与初始页面内容 |
| `migration_home_hero_author_sidebar.sql` / `migration_home_hero_author_columns_if_missing.sql` | 为旧首页表补充作者字段，按现有字段选择 |
| `migration_about_page_settings.sql` | 关于页面配置 |
| `migration_site_settings.sql` | 站点设置 |
| `migration_guest_message_interactions.sql` / `migration_guest_message_interactions_safe.sql` | 留言互动字段和评论表，选择适用版本 |
| `migration_site_music_track.sql` / `migration_site_music_track_meta.sql` | 音乐表及元数据字段，先检查基础表已有字段 |
| `migration_user_qq_openid.sql` | QQ OpenID 字段，启用 QQ 登录或使用相关用户插入逻辑前核对 |
| `db/migration_hobby_article_cover_image.sql` | 专栏文章封面字段 |

这些脚本不是自动版本化迁移系统，部分包含重复字段或互为替代版本。先阅读 SQL，再比较已有表结构；不要一次性执行整个目录。已有数据库应先备份。

## 配置与启动后端

### 必填变量

| 变量 | 说明 |
|---|---|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | 数据库用户名 |
| `DB_PASSWORD` | 数据库密码 |
| `JWT_SECRET` | 自己生成的随机密钥，至少 32 个 UTF-8 字节 |
| `OPENAI_API_KEY` 或 `DASHSCOPE_API_KEY` | AI 服务密钥；两者同时设置时后者优先 |
| `AI_MODEL` | 服务商支持的模型名称 |

### 可选变量

| 变量 | 默认值或用途 |
|---|---|
| `AI_BASE_URL` | `https://api.openai.com`，使用其他兼容服务时修改 |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | 空 |
| `APP_UPLOAD_DIR` | 相对目录 `uploads`；生产环境建议绝对路径 |
| `QQ_ENABLED` | `false`，其他 QQ 变量见下文 |

```powershell
cd backend
$env:DB_URL = 'jdbc:mysql://localhost:3306/blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
$env:DB_USERNAME = '你的数据库用户名'
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = '替换为你自己生成的至少32字节随机密钥'
$env:OPENAI_API_KEY = '你的API密钥'
$env:AI_BASE_URL = 'https://api.openai.com'
$env:AI_MODEL = '服务商支持的模型名称'
mvn spring-boot:run
```

默认后端监听 `8080`。示例文字必须替换成自己的值，JWT 密钥不要直接复制示例作为真实密钥；更换 JWT 密钥会让旧登录令牌失效。

也可以创建被 Git 忽略的 `backend/src/main/resources/application-local.yml`，在其中填写 Spring 属性，并运行：

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

后端 **不会自动读取 `.env` 文件**。完整说明见 [后端配置文档](backend/CONFIGURATION.md)。

## 创建管理员

仓库没有默认管理员、默认密码，也不会在数据库找不到用户时返回内置管理员。

在自己的数据库客户端执行以下模板，填写自己的账号密码：

```sql
INSERT INTO `user` (`username`, `password`, `role`)
VALUES ('自行填写用户名', '自行设置密码', 'admin');
```

然后访问前端 `/admin/login` 登录。

**当前代码使用明文密码比较**，因此示例中的数据库密码值需要与输入一致。不要误以为已有密码哈希验证；生产部署前应修改存储和验证逻辑，使用可靠的密码哈希方案。实际账号和密码不要提交到 SQL 脚本或说明文档。

## 配置与启动前端

在另一个终端，从仓库根目录执行：

```powershell
cd frontend
npm ci
Copy-Item .env.example .env.local
npm run dev
```

访问 `http://localhost:5173`。开发服务器的 `/api` 与 `/uploads` 默认代理到 `http://localhost:8080`，其中 `/api` 前缀会被移除，例如 `/api/posts` 对应后端 `/posts`。

在 `.env.local` 中按需要填写公开备案信息：

```dotenv
VITE_SITE_ICP=
VITE_SITE_PSB=
VITE_SITE_PSB_URL=
```

留空会隐藏对应备案信息。修改环境变量后重启开发服务器，或重新构建生产包。前端所有 `VITE_*` 变量都可能进入浏览器代码，不能放数据库密码、JWT 密钥或服务 API Key。

## QQ 登录

默认关闭。启用前，需要自行申请 QQ 应用并设置：

| 变量 | 内容 |
|---|---|
| `QQ_ENABLED` | `true` |
| `QQ_APP_ID` / `QQ_APP_KEY` | 自己申请的应用凭据 |
| `QQ_REDIRECT_URI` | QQ 平台登记的完整回调地址 |
| `QQ_FRONTEND_SUCCESS` / `QQ_FRONTEND_ERROR` | 成功和失败后跳转的前端页面 |
| `QQ_ADMIN_OPENIDS` | 可登录后台的 OpenID，多个用英文逗号分隔 |

通过前端 `/api` 反向代理部署时，公开回调一般为 `https://你的域名/api/auth/qq/callback`；后端真实路由为 `/auth/qq/callback`。必须与 QQ 平台登记地址匹配。管理员 OpenID 留空时不允许 QQ 登录后台。

## 自定义内容与素材

- 首页和作者：后台首页配置页面 `/admin/home-hero`；默认昵称和博客名仍有源码兜底值，可自行替换。
- 关于页面：`/admin/about`；图标和站点设置：`/admin/site`。
- 头像、背景：`frontend/src/assets/` 与 `frontend/public/`，修改文件时同步检查代码引用。
- 示例音乐：`frontend/src/config/musicPlaylist.js`；也可通过音乐后台管理自己的播放列表。
- 上传文件：保存至 `APP_UPLOAD_DIR`，通过后端 `/uploads/**` 提供访问；文件不会自动上传到 GitHub。

上传接口目前限制图片 8MB、视频 120MB，Spring 请求上限为 130MB。代理层也应设置适当请求大小。

仓库中的图片可以随克隆获取。音乐默认使用远程演示链接，仓库未附音频文件；链接可用性与素材授权需由使用者自行确认。不要提交私人照片、数据库导出、上传文件或含个人内容的知识库资料。

## 构建与部署

### 验证与打包

```powershell
# backend 目录
mvn test
mvn package
# frontend 目录
npm run build
```

后端打包产物当前为 `backend/target/demo-0.0.1-SNAPSHOT.jar`，修改版本号后文件名会变化。前端产物位于 `frontend/dist/`。

### 后端运行

在服务启动环境中设置前面的变量，然后运行：

```powershell
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

生产环境应持久保存上传目录、备份数据库，并将后端交由服务管理工具运行。

### 前端与代理

将 `frontend/dist/` 部署到静态服务器。Vite 开发代理不会进入生产包，需要配置服务器代理。以下 Nginx 示例中的域名、目录和端口需自行修改，并另行配置 HTTPS：

```nginx
server {
    listen 80;
    server_name example.com;
    root /srv/blog/frontend/dist;
    client_max_body_size 130m;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080;
    }
}
```

`/api/` 的 `proxy_pass` 末尾斜杠用于去掉 API 前缀；`/uploads/` 保留原路径。`try_files` 用于支持 Vue history 路由，避免刷新文章详情页时出现 404。此示例未在你的服务器上实际部署验证。

## 常见问题

| 现象 | 排查方向 |
|---|---|
| 提示无法解析 `DB_URL`、`JWT_SECRET` 或 `AI_MODEL` | 变量未设置，或启动命令不在同一个终端；本地配置文件需要启用 local profile |
| JWT 密钥长度报错 | 使用至少 32 个 UTF-8 字节的随机密钥 |
| 数据库拒绝连接或账号访问失败 | 检查 MySQL 状态、数据库名、凭据和账号权限 |
| `Unknown column` / 表不存在 | 对照 mapper 和数据库结构检查缺少的迁移，不要反复执行全部 SQL |
| AI Key 报空值或请求 401 | 检查密钥、服务商地址和模型；当前 AI 未实现一键禁用 |
| 前端接口返回 502 或连接失败 | 后端是否启动、端口是否正确、开发或生产代理是否配置 |
| 管理员登录失败 | 数据库是否有 role 为 admin 的账号，输入密码与当前明文值是否一致 |
| QQ 登录按钮不显示或回调失败 | 检查启用开关、应用凭据、登记回调及管理员 OpenID |
| 刷新页面出现 404 | 静态服务器需回退到 index.html |
| 上传失败 | 检查目录写权限、文件类型、接口大小限制和代理大小限制 |
| 构建提示 chunk 大于 500kB | 当前有较大前端包，属于构建警告，可另行按需拆分 |

## 提交文件与当前限制

可以提交源码、`package-lock.json`、`pom.xml`、脱敏公共配置、`.env.example` 和文档。真实环境文件、本地配置、工具个人设置、`node_modules`、`target`、`dist` 和上传目录已加入忽略规则。已跟踪文件不会因新增忽略规则自动移出 Git。

此仓库尚不是经过完整生产安全加固的成品：密码验证仍为明文比较，跨域规则允许所有来源，错误信息默认可返回客户端，部分初始化脚本仍需手动选择。上线前应完成认证和授权审查、密码哈希改造、跨域限制、HTTPS、数据库与文件备份及日志管理。

已有后端测试和前端构建通过记录，但不代表所有页面及生产部署场景都经过验证。仓库当前没有 LICENSE，不能据此推定已授予自由分发、商用或修改授权。
