# 个人博客

基于 Vue 3 + Vite 和 Spring Boot 3 的前后端分离博客，包含文章、评论、留言板、爱好、归档、音乐播放和管理后台，以及可选 QQ 登录与 AI 服务。

## 项目结构

```text
backend/             Java 后端与数据库脚本
frontend/            Vue 前端
backend/CONFIGURATION.md  环境变量和数据库配置说明
frontend/.env.example    前端公开配置示例
```

## 环境要求

- Java 17、Maven 3.9
- Node.js 18+、npm
- MySQL 8；按实际功能配置 Redis
- 当前后端 AI 组件随应用启动，需要配置兼容 OpenAI 的 API 服务

## 启动后端

创建数据库并阅读 `backend/src/main/resources/sql/` 中的基础表和迁移脚本，按需初始化。详细步骤见 [配置说明](backend/CONFIGURATION.md)。仓库没有默认管理员账号，需要自行在数据库创建。

在 PowerShell 中设置配置后启动：

```powershell
cd backend
$env:DB_URL = 'jdbc:mysql://localhost:3306/blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
$env:DB_USERNAME = '你的数据库用户名'
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = '自行生成至少32字节的随机密钥'
$env:OPENAI_API_KEY = '你的API密钥'
$env:AI_BASE_URL = 'https://api.openai.com'
$env:AI_MODEL = '服务商支持的模型名称'
mvn spring-boot:run
```

默认后端地址：`http://localhost:8080`。真实密码与密钥只存于本地环境变量或被忽略的本地配置文件。

## 启动前端

```powershell
cd frontend
npm ci
Copy-Item .env.example .env.local
npm run dev
```

默认前端地址：`http://localhost:5173`。开发代理将 `/api` 和 `/uploads` 转发到后端。备案信息可在 `.env.local` 中自行配置，留空时隐藏。

`VITE_*` 变量会暴露在浏览器中，只能配置公开信息。

## 构建与检查

```powershell
# backend 目录
mvn test
mvn package
# frontend 目录
npm run build
```

前端构建产物位于 `frontend/dist`，生产环境需自行配置静态站点和 API 反向代理。QQ 登录的应用信息、回调地址及管理员 OpenID 必须使用部署者自己的配置。

## 使用说明

- 当前账号密码验证使用明文比较，生产部署前需改为密码哈希验证。
- 当前跨域规则允许所有来源，生产部署时按实际域名收紧。
- 初始化 SQL 和迁移脚本不是完整的自动迁移系统；对已有数据库先备份，再按表结构选择脚本。
- 首页的名称、头像、背景和示例音乐可按自己的需求替换；发布素材前确认使用权限。
- 仓库未附开源许可证；如需授权他人分发或修改，请补充适合的 LICENSE。
