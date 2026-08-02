# 古籍食养

古籍食养是一个前后端分离的食养知识平台，围绕传统食疗、古籍菜谱、体质测试、知识图谱、论坛互动和后台管理构建。项目包含 Vue 3 前端、Spring Boot 后端，以及 MySQL、Redis、RabbitMQ、Neo4j、Chroma 等配套服务。

## 功能概览

- 古籍与章节浏览：古籍列表、详情、章节内容展示
- 食养菜品与食材：菜品、食材、菜品-食材关系、古籍出处关系
- 体质测试与调理建议：中医九大体质测试、分析缓存、饮食建议
- 智能食疗问答与方案：基于大模型和 RAG 知识库的问答、膳食计划生成
- 知识图谱：Neo4j 支撑的食材、菜品、古籍关联图谱
- 社区论坛：发帖、评论、点赞、收藏、关注、消息通知
- 个人中心：资料编辑、收藏、体质报告、打卡记录
- 后台管理：用户、古籍、菜品、食材、论坛、消息、反馈和数据看板管理
- 语音与图像能力：讯飞语音识别、火山方舟图片生成相关接口

## 技术栈

前端：

- Vue 3
- Vite 6
- TypeScript
- Vue Router
- Pinia
- Element Plus
- ECharts / D3
- Tailwind CSS
- Axios

后端：

- Java 17
- Spring Boot 3.4
- Spring AI
- MyBatis / MyBatis Plus
- MySQL 8
- Redis
- RabbitMQ
- Neo4j
- Chroma Vector Store
- Knife4j / OpenAPI

## 目录结构

```text
.
├── backend/                 # Spring Boot 后端服务
│   ├── src/main/java/       # 业务代码
│   ├── src/main/resources/  # 配置、AI prompt、知识库文件
│   ├── uploads/             # 本地上传文件目录
│   ├── Dockerfile
│   └── .env.example
├── frontend/                # Vue 3 前端应用
│   ├── src/
│   ├── public/
│   ├── Dockerfile
│   ├── nginx.conf.template
│   └── .env.example
├── deploy/mysql/init/       # MySQL 初始化 SQL
├── docker-compose.yml       # Docker Compose 编排
├── docker.env.example       # Docker 环境变量模板
└── DOCKER.md                # Docker 部署补充说明
```

## 环境要求

本地开发：

- JDK 17
- Maven 3.8+
- Node.js 22+
- pnpm 或 npm
- MySQL 8
- Redis
- RabbitMQ
- Neo4j 5
- Chroma

Docker 部署：

- Docker
- Docker Compose

## 快速启动：Docker Compose

推荐使用 Docker Compose 启动完整依赖环境。

1. 复制环境变量：

```bash
copy docker.env.example .env
```

Linux/macOS：

```bash
cp docker.env.example .env
```

2. 编辑 `.env`，至少确认以下配置：

```text
MYSQL_ROOT_PASSWORD
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
NEO4J_USERNAME
NEO4J_PASSWORD
FILE_BASE_URL
SILICONFLOW_API_KEY
DEEPSEEK_API_KEY
ARK_API_KEY
XFYUN_SPEECH_APP_ID
XFYUN_SPEECH_API_KEY
XFYUN_SPEECH_API_SECRET
VITE_QWEATHER_API_KEY
QWEATHER_API_HOST
```

没有接入对应第三方服务时，相关 AI、语音、天气能力可能不可用，但基础页面和数据库功能仍可按配置运行。

3. 启动服务：

```bash
docker compose up -d --build
```

4. 访问前端：

```text
http://localhost
```

默认服务说明：

- 前端 Nginx：`http://localhost`
- 后端容器内部端口：`8081`
- 前端会通过 Nginx 反向代理访问 `/api`、`/uploads`、`/ws`
- 天气接口通过 `/weather-api` 反向代理到 `QWEATHER_API_HOST`

常用命令：

```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose down
docker compose down -v
```

`docker compose down -v` 会删除数据库、Redis、RabbitMQ、Neo4j、Chroma 等数据卷，请谨慎使用。

## 本地开发

### 1. 数据库与中间件

后端默认读取 `backend/.env`、根目录 `.env` 或系统环境变量。可参考：

```bash
copy backend\.env.example backend\.env
```

需要准备：

- MySQL 数据库，默认库名为 `lyfw`
- Redis，默认 `127.0.0.1:6379`
- RabbitMQ，默认 `127.0.0.1:5672`
- Neo4j，默认 `bolt://localhost:7687`
- Chroma，默认 `http://localhost:8000`

MySQL 初始化 SQL 位于：

```text
deploy/mysql/init/
```

其中包含用户表初始化和业务数据表初始化脚本。

### 2. 启动后端

```bash
cd backend
copy .env.example .env
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8081
```

接口文档通常可通过以下地址查看：

```text
http://localhost:8081/doc.html
http://localhost:8081/swagger-ui/index.html
```

### 3. 启动前端

```bash
cd frontend
copy .env.example .env
pnpm install
pnpm run dev
```

如果使用 npm：

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器默认端口：

```text
http://localhost:3001
```

前端开发代理默认把 `/api`、`/uploads` 转发到：

```text
http://localhost:8081
```

可在 `frontend/.env` 中覆盖：

```text
VITE_DEV_SERVER_PROXY_TARGET=http://localhost:8081
VITE_DEV_SERVER_PORT=3001
VITE_WS_BASE_URL=ws://localhost:8081
VITE_QWEATHER_API_KEY=
VITE_QWEATHER_API_HOST=
```

## 构建

前端构建：

```bash
cd frontend
pnpm run build:client
```

后端打包：

```bash
cd backend
mvn clean package
```

跳过测试打包：

```bash
mvn clean package -DskipTests
```

## 关键配置说明

后端常用环境变量：

```text
SERVER_PORT
MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
RABBITMQ_HOST
NEO4J_URI
CHROMA_HOST
CHROMA_PORT
FILE_BASE_URL
FILE_UPLOAD_DIR
```

AI 与第三方服务配置：

```text
SILICONFLOW_API_KEY
DEEPSEEK_API_KEY
ARK_API_KEY
XFYUN_SPEECH_APP_ID
XFYUN_SPEECH_API_KEY
XFYUN_SPEECH_API_SECRET
VITE_QWEATHER_API_KEY
QWEATHER_API_HOST
```

RAG 相关配置：

```text
RAG_INDEX_ON_STARTUP
RAG_CLEAR_BEFORE_INDEX
RAG_KNOWLEDGE_LOCATION
RAG_INDEX_BATCH_SIZE
RAG_SEARCH_TOP_K
RAG_SEARCH_SIMILARITY_THRESHOLD
```

默认知识库文件位于：

```text
backend/src/main/resources/knowledge/
```

## 常见问题

### 前端请求后端失败

确认后端已启动，并检查 `frontend/.env`：

```text
VITE_DEV_SERVER_PROXY_TARGET=http://localhost:8081
```

Docker 环境下前端使用 Nginx 代理 `/api` 到后端容器，不需要配置浏览器直连后端地址。

### 上传文件访问失败

确认 `FILE_BASE_URL` 和 `FILE_UPLOAD_DIR` 配置正确。Docker 环境下上传目录挂载到：

```text
backend/uploads
```

### MySQL 初始化脚本没有重新执行

MySQL 官方镜像只会在首次创建数据卷时执行 `/docker-entrypoint-initdb.d` 下的 SQL。已有数据卷不会重复执行初始化脚本。如需完全重建数据，请先备份，再执行：

```bash
docker compose down -v
docker compose up -d --build
```

### RAG 初始化较慢或失败

如果首次启动需要索引知识库，启动时间会变长。可通过以下变量控制行为：

```text
RAG_INDEX_ON_STARTUP
RAG_CLEAR_BEFORE_INDEX
RAG_FAIL_ON_INDEX_ERROR
```

## 开发备注

- 根目录 `.env`、`backend/.env`、`frontend/.env` 用于本地配置，不应提交真实密钥。
- `rag-env/`、`chroma_data/`、上传文件和截图属于运行或调试产物，通常不作为源码维护。
- Docker 前端镜像使用 `npm run build:client`，避免完整 `npm run build` 中的 Windows 专用命令影响 Linux 容器构建。
