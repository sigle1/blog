# 自行配置与运行

仓库不提供数据库密码、JWT 密钥、QQ 密钥或默认管理员。后端使用 Java 17、Maven、MySQL；前端使用 Node.js、Vue 3 和 Vite。

## 后端

在启动后端的 PowerShell 窗口设置以下变量（把占位说明替换为自己的值）：

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/blog?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
$env:DB_USERNAME = '你的数据库用户名'
$env:DB_PASSWORD = '你的数据库密码'
$env:JWT_SECRET = '自行生成的至少32字节随机密钥'
$env:AI_BASE_URL = 'https://api.openai.com'
$env:OPENAI_API_KEY = '你的服务商API密钥'
$env:AI_MODEL = '服务商支持的模型名称'
mvn spring-boot:run
```

JWT 密钥必须至少 32 个 UTF-8 字节；不同实例应使用同一密钥。更换密钥会使之前签发的登录令牌失效。

兼容服务商可设置自己的 `AI_BASE_URL` 和 `AI_MODEL`；也可使用 `DASHSCOPE_API_KEY`，它优先于 `OPENAI_API_KEY`。目前 AI 组件仍随应用启动，因此必须提供有效的服务配置，未实现自动禁用 AI。

可选变量：

| 变量 | 默认值或用途 |
|---|---|
| REDIS_HOST / REDIS_PORT | localhost / 6379 |
| REDIS_PASSWORD | 空，按实际 Redis 配置设置 |
| APP_UPLOAD_DIR | uploads，生产环境设置可写的持久化目录 |
| QQ_ENABLED | false |
| QQ_APP_ID / QQ_APP_KEY | 自己申请的 QQ 应用配置 |
| QQ_REDIRECT_URI | 本地回调地址；部署时填写 QQ 平台登记的完整回调地址 |
| QQ_FRONTEND_SUCCESS / QQ_FRONTEND_ERROR | 本地前端登录页；部署时填写自己的地址 |
| QQ_ADMIN_OPENIDS | 允许访问管理后台的 QQ OpenID，逗号分隔 |

Spring Boot 不会自动加载 `.env`。请使用系统环境变量，或创建被忽略的 `src/main/resources/application-local.yml`，使用 `mvn spring-boot:run "-Dspring-boot.run.profiles=local"` 启用。不要把真实配置写回公开的 application.yml。

## 数据库与管理员

先创建 MySQL 数据库，再执行 `src/main/resources/sql/create_tables.sql`。该文件包含基础表；按实际缺少的表/列阅读并执行目录下适用的迁移脚本，先备份已有数据库，不要盲目重复执行迁移。

不再提供任何默认管理员或账号兜底。当前用户名密码登录代码仍使用明文比较，数据库中的 password 必须与输入一致；公开生产部署前应另行改为可靠的密码哈希验证。可在自己的数据库客户端执行以下模板创建管理员，真实密码不要保存到仓库：

```sql
INSERT INTO `user` (`username`, `password`, `role`)
VALUES ('自行填写用户名', '自行设置密码', 'admin');
```

## 前端

进入前端目录，运行 `npm ci` 和 `npm run dev`。默认访问 http://localhost:5173，`/api` 与 `/uploads` 代理至 http://localhost:8080。部署时按自己的站点配置反向代理。

复制 `.env.example` 为 `.env.local`，按需要填写 `VITE_SITE_ICP`、`VITE_SITE_PSB`、`VITE_SITE_PSB_URL`。留空时隐藏相应备案信息。修改后重启开发服务或重新构建。

所有 `VITE_*` 变量会进入浏览器代码，只能放公开信息，不能放数据库密码、API Key 或 JWT 密钥。

## 上传前

此仓库 backend/ 为后端，frontend/ 为前端；分别进入对应目录运行命令。

环境文件、本地配置、工具个人设置、依赖、构建产物和上传文件不应提交。源码、package-lock.json、pom.xml、脱敏的公共配置和说明文件可以提交。头像、图片、音乐和知识库资料请自行确认是否愿意公开以及是否有发布权限。

本次未进行提交或上传。若曾把真实凭据分享给他人或上传，删除文件不能撤销凭据，应在相应服务中更换。
